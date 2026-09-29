import logging
from datetime import datetime, timezone

import requests
from app.config import settings
from app.ingestion.chunker import filter_out_decorative_chunks, generate_chunks
from app.ingestion.extractor import extract_text
from app.ingestion.vectoriser import vectorise_chunks
from app.services import lancedb_service, mongo_service, r2_service
from bson import ObjectId
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def migrate_rulebooks(force: bool = False):  # NOSONAR
    """
    Full re-embedding migration. Extracts, chunks, and vectorises all Ready rulebooks.
    Skips:
        - rulebooks actively locked by an editing session.
        - rulebooks that already have 'lancedbMigratedAt' set (unless force=True)
    """
    db = mongo_service.get_db()
    model = SentenceTransformer(
        "nomic-ai/nomic-embed-text-v1.5",
        device="cpu",
        revision="e9b6763023c676ca8431644204f50c2b100d9aab",
        trust_remote_code=True,
    )

    if force:
        query = {"status": "Ready"}
    else:
        query = {
            "status": "Ready",
            "$or": [
                {"lancedbMigratedAt": {"$exists": False}},
                {"lancedbMigratedAt": None},
            ],
        }

    total_to_migrate = db["RULEBOOK"].count_documents(query)
    logger.info("Found %d rulebooks pending migration.", total_to_migrate)

    cursor = db["RULEBOOK"].find(query, batch_size=50)
    skipped_locked: list[str] = []
    migrated: list[str] = []
    failed: list[tuple[str, str]] = []

    for rulebook in cursor:
        rulebook_id = str(rulebook["_id"])
        now = datetime.now(timezone.utc)

        lock_held_by = rulebook.get("lockHeldBy")
        lock_expires_at = rulebook.get("lockExpiresAt")

        if (lock_held_by and lock_expires_at) and lock_expires_at.replace(
            tzinfo=timezone.utc
        ) > now:
            logger.warning(
                "Skipping rulebook %s - currently locked by %s",
                rulebook_id,
                lock_held_by,
            )
            skipped_locked.append(rulebook_id)
            continue

        logger.info("Migrating rulebook: %s", rulebook_id)
        try:
            pdf_bytes = r2_service.download_from_r2(rulebook.get("r2PdfKey"))
            if not pdf_bytes:
                logger.error("Failed to fetch PDF for %s. Skipping.", rulebook_id)
                failed.append((rulebook_id, "missing_pdf"))
                continue

            extract_success, _, _, blocks_cache = extract_text(pdf_bytes, rulebook_id)
            if not extract_success:
                logger.error("Extraction failed for %s. Skipping.", rulebook_id)
                failed.append((rulebook_id, "extract_failed"))
                continue

            chunk_success, new_chunks, _ = generate_chunks(blocks_cache)
            if not chunk_success or not new_chunks:
                logger.error("Chunking failed for %s. Skipping.", rulebook_id)
                failed.append((rulebook_id, "chunking_failed"))
                continue

            new_chunks = filter_out_decorative_chunks(new_chunks)
            if not new_chunks:
                logger.error("All chunks filtered as decorative for %s.", rulebook_id)
                failed.append((rulebook_id, "all_decorative"))
                continue

            for chunk in new_chunks:
                # Normalise chunkId to string for the vectoriser and lanceDB
                chunk["chunkId"] = str(chunk["chunkId"])

            vec_success, vectorised_chunks, _ = vectorise_chunks(new_chunks, model)
            if not vec_success:
                logger.error("Vectorisation failed for %s. Skipping.", rulebook_id)
                failed.append((rulebook_id, "vectorise_failed"))
                continue

            current_time = datetime.now(timezone.utc)
            for chunk in vectorised_chunks:
                chunk["rulebookId"] = rulebook_id
                chunk.setdefault("createdAt", current_time)
                chunk["updatedAt"] = current_time

            # Mongo RULEBOOK_TEXT replacement
            mongo_service.replace_rulebook_text(rulebook_id, vectorised_chunks)

            # LanceDB Replacement
            existing_lance_ids = lancedb_service.get_all_chunk_ids_for_rulebook(
                rulebook_id
            )
            if existing_lance_ids:
                lancedb_service.delete_chunks(list(existing_lance_ids))

            lancedb_service.write_chunks(vectorised_chunks)

            db["RULEBOOK"].update_one(
                {"_id": ObjectId(rulebook_id)},
                {"$set": {"lancedbMigratedAt": datetime.now(timezone.utc)}},
            )

            migrated.append(rulebook_id)
            logger.info("Successfully migrated %s", rulebook_id)

        except Exception:
            logger.exception("Unexpected error migrating %s", rulebook_id)
            failed.append((rulebook_id, "unexpected_exception"))
    if skipped_locked:
        logger.info(
            "Run complete. %d rulebooks were skipped due to active locks.",
            len(skipped_locked),
        )
        logger.info("Skipped IDs: %s", skipped_locked)

    logger.info(
        "Migration summary: migrated=%d skipped_locked=%d failed=%d",
        len(migrated),
        len(skipped_locked),
        len(failed),
    )
    for rid, reason in failed:
        logger.error("FAILED %s: %s", rid, reason)

    if migrated:
        logger.info("Migration batch complete. Initiating LanceDB index rebuild")
        lancedb_service.ensure_indexes(force_recreate=True)

        logger.info("Notifying API to clear table cache.")
        try:
            api_url = "http://localhost:8000/api/fa/vault/internal/lancedb/clear-cache"

            if settings.INTERNAL_WEBHOOK_SECRET is None:
                raise ValueError(
                    "INTERNAL_WEBHOOK_SECRET environment variable is not set"
                )

            response = requests.post(
                api_url, headers={"X-Internal-Token": settings.INTERNAL_WEBHOOK_SECRET}
            )
            response.raise_for_status()
            logger.info("Successfully cleared API cache.")
        except Exception:
            logger.exception("Failed to clear API cache")
    else:
        logger.info("No rulebooks migrated; skipping index rebuild and cache clear.")

    logger.info("Migration script finished.")


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Re-embed rulebooks into LanceDB.")
    parser.add_argument(
        "--force",
        action="store_true",
        help="Re-migrate rulebooks even if lancedbMigratedAt is already set.",
    )
    args = parser.parse_args()
    migrate_rulebooks(force=args.force)
