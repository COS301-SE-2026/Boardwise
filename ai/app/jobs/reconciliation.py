import logging

from bson import ObjectId

from app.ingestion.vectoriser import vectorise_chunks
from app.services import lancedb_service, mongo_service

logger = logging.getLogger(__name__)


def reconcile_rulebook(rulebook_id: str, embedding_model) -> None:
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
