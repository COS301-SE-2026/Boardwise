import logging

from bson import ObjectId
from sentence_transformers import SentenceTransformer

from app.ingestion.vectoriser import vectorise_chunks
from app.services import lancedb_service, mongo_service

logger = logging.getLogger(__name__)


def run_reconciliation_sweep(embedding_model) -> None:
    """Cron job to detect and repair orphaned chunks"""
    db = mongo_service.get_db()
    query = {"status": "Ready"}
    rulebook_ids = [str(r["_id"]) for r in db["RULEBOOK"].find(query, {"_id": 1})]

    logger.info("Starting reconciliation sweep for %d rulebooks.", len(rulebook_ids))
    for rulebook_id in rulebook_ids:
        try:
            _reconcile_rulebook(rulebook_id, embedding_model)
        except Exception:
            logger.exception("Reconciliation failed for rulebook %s", rulebook_id)
    logger.info("Reconciliation sweep complete.")


def _reconcile_rulebook(rulebook_id: str, embedding_model) -> None:
    """
    Detects and repairs orphaned chunks
    """
    db = mongo_service.get_db()

    mongo_docs = list(db["RULEBOOK_TEXT"].find({"rulebookId": ObjectId(rulebook_id)}))
    mongo_chunk_ids = {str(doc["_id"]) for doc in mongo_docs}
    mongo_doc_map = {str(doc["_id"]): doc for doc in mongo_docs}

    lancedb_chunk_ids = lancedb_service.get_all_chunk_ids_for_rulebook(rulebook_id)

    mongo_orphans = mongo_chunk_ids - lancedb_chunk_ids
    if mongo_orphans:
        logger.info(
            "Found %d Mongo orphans for rulebook %s. Initiating re-vectorisation",
            len(mongo_orphans),
            rulebook_id,
        )
        chunks_to_embed = []
        for cid in mongo_orphans:
            doc = mongo_doc_map[cid]
            chunks_to_embed.append(
                {
                    "chunkId": cid,
                    "rulebookId": rulebook_id,
                    "index": doc["index"],
                    "content": doc["content"],
                    "type": doc.get("type", "text"),
                    "needsReview": doc.get("needsReview", False),
                    "confidence": doc.get("confidence", 1.0),
                    "associatedImageUrls": doc.get("associatedImageUrls", []),
                    "charCount": doc["charCount"],
                    "createdAt": doc["createdAt"],
                    "updatedAt": doc["updatedAt"],
                }
            )
        success, vectorised_chunks, reason = vectorise_chunks(
            chunks_to_embed, embedding_model
        )
        if success:
            lancedb_service.write_chunks(vectorised_chunks)
        else:
            logger.error("Failed to vectorise Mongo orphans: %s", reason)

    lancedb_orphans = lancedb_chunk_ids - mongo_chunk_ids
    if lancedb_orphans:
        logger.info(
            "Found %d LanceDB orphans for rulebook %s. Initiating orphan removal",
            len(lancedb_orphans),
            rulebook_id,
        )
        lancedb_service.delete_chunks(list(lancedb_orphans))

    if not mongo_orphans and not lancedb_orphans:
        logger.info("Rulebooks %s has been reconcile.", rulebook_id)

if __name__ == "__main__":
    model = SentenceTransformer(
        "nomic-ai/nomic-embed-text-v1.5",
        device="cpu",
        revision="e9b6763023c676ca8431644204f50c2b100d9aab",
        trust_remote_code=True,
    )
    run_reconciliation_sweep(model)
