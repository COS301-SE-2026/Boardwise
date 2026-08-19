import logging

from app.services import mongo_service

logger = logging.getLogger(__name__)


def fetch_candidate_chunks(
    rulebook_id: str, query_vector: list[float], limit: int = 15
) -> list[dict]:
    """
    Executes a vector search against MongoDB Atlas to find the most relevant rulebook chunks.
    Filters strictly by rulebookId and drops the raw vector from the response to preserve RAM.
    """
    try:
        pipeline = [
            {
                "$vectorSearch": {
                    "index": "vector_index",
                    "path": "embedding",
                    "queryVector": query_vector,
                    "numCandidates": 50,
                    "limit": limit,
                    "filter": {"rulebookId": rulebook_id},
                }
            },
            {
                "$project": {
                    "_id": 1,
                    "chunkId": 1,
                    "content": 1,
                    "index": 1,
                    "charCount": 1,
                    "score": {"$meta": "vectorSearchScore"},
                }
            },
        ]

        db = mongo_service.client.get_default_database()
        collection = db["RULEBOOK_TEXT"]

        results = list(collection.aggregate(pipeline))

        logger.info(
            "Successfully retrieved %d candidate chunks for rulebook %s",
            len(results),
            rulebook_id,
        )
        return results
    except Exception:
        logger.exception("Failed to execute vector search for rulebook %s", rulebook_id)
        raise
