import logging
from datetime import datetime, timezone

from app.ingestion.chunker import filter_out_decorative_chunks, generate_chunks
from app.ingestion.extractor import extract_text
from app.ingestion.vectoriser import vectorise_chunks
from app.services import lancedb_service, mongo_service, r2_service
from bson import ObjectId
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def migrate_rulebooks():
    """
    Full re-embedding migration. Extracts, chunks, and vectorises all Ready rulebooks.
    Skips actively locked rulebooks to prevent editing session corruption
    """
    db = mongo_service.get_db()
    model = SentenceTransformer(
        "nomic-ai/nomic-embed-text-v1.5",
        device="cpu",
        revision="e9b6763023c676ca8431644204f50c2b100d9aab",
        trust_remote_code=True,
    )

    query = {"status": "Ready", "lancedbMigratedAt": {"$exists": False}}

    total_to_migrate = db["RULEBOOK"].count_documents(query)
    logger.info(f"Found {total_to_migrate} rulebooks pending migration.")

    cursor = db["RULEBOOK"].find(query, batch_size=50)
    skipped_locked = []

    for rulebook in cursor:
        rulebook_id = str(rulebook["_id"])
        now = datetime.now(timezone.utc)

        lock_held_by = rulebook.get("lockHeldBy")
        lock_expires_at = rulebook.get("lockExpiresAt")

        if (lock_held_by and lock_expires_at) and lock_expires_at.replace(
            tzinfo=timezone.utc
        ) > now:
            logger.warning(
                f"Skipping rulebook {rulebook_id} - currently locked by {lock_held_by}"
            )
            skipped_locked.append(rulebook_id)
            continue

        logger.info(f"Migrating rulebook: {rulebook_id}")
        try:
            pdf_bytes = r2_service.download_from_r2(rulebook.get("r2PdfKey"))
            if not pdf_bytes:
                logger.error(f"Failed to fetch PDF for {rulebook_id}. Skipping.")
                continue

            extract_success, _, _, blocks_cache = extract_text(pdf_bytes, rulebook_id)
            if not extract_success:
                logger.error(f"Extraction failed for {rulebook_id}. Skipping.")
                continue

            chunk_success, new_chunks, _ = generate_chunks(blocks_cache)
            if not chunk_success or not new_chunks:
                logger.error(f"Chunking failed for {rulebook_id}. Skipping.")
                continue

            new_chunks = filter_out_decorative_chunks(new_chunks)

            for chunk in new_chunks:
                # Normalise chunkId to string for the vectoriser and lanceDB
                chunk["chunkId"] = str(chunk["chunkId"])

            vec_success, vectorised_chunks, _ = vectorise_chunks(new_chunks, model)
            if not vec_success:
                logger.error(f"Vectorisation failed for {rulebook_id}. Skipping.")
                continue

            client = db.client
            with client.start_session() as session, session.start_transaction():
                db["RULEBOOK_TEXT"].delete_many(
                    {"rulebookId": ObjectId(rulebook_id)}, session=session
                )

                mongo_chunks = []
                current_time = datetime.now(timezone.utc)
                for chunk in vectorised_chunks:
                    mongo_chunks.append(
                        {
                            "_id": ObjectId(chunk["chunkId"]),
                            "rulebookId": ObjectId(rulebook_id),
                            "index": chunk["index"],
                            "content": chunk["content"],
                            "charCount": len(chunk["content"]),
                            "type": chunk.get("type", "text"),
                            "needsReview": chunk.get("needsReview", False),
                            "confidence": chunk.get("confidence", 1.0),
                            "associatedImageUrls": chunk.get("associatedImageUrls", []),
                            "createdAt": current_time,
                            "updatedAt": current_time,
                        }
                    )
                if mongo_chunks:
                    db["RULEBOOK_TEXT"].insert_many(mongo_chunks, session=session)

            existing_lance_ids = lancedb_service.get_all_chunk_ids_for_rulebook(
                rulebook_id
            )
            if existing_lance_ids:
                lancedb_service.delete_chunks(list(existing_lance_ids))

            lancedb_service.write_chunks(vectorised_chunks)

            db["RULEBOOK"].update_one(
                {"_id": ObjectId(rulebook_id)},
                {"$set": {"lancedbMigratedAt": current_time}},
            )
            logger.info(f"Successfully migrated {rulebook_id}")

        except Exception:
            logger.exception(f"Unexpected error migrating {rulebook_id}")
    if skipped_locked:
        logger.info(
            f"Run complete. {len(skipped_locked)} rulebooks were skipped due to acive locks."
        )
        logger.info(f"Skipped IDs: {skipped_locked}")

    logger.info("Migration batch complete. Initiating LanceDB index rebuild")
    lancedb_service.ensure_indexes(force_recreate=True)
    logger.info("Migration script finished.")


if __name__ == "__main__":
    migrate_rulebooks()
