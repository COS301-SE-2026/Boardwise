import logging

from bson import ObjectId

from app.services import mongo_service
from app.utils.logging_utils import sanitise_log_input

logger = logging.getLogger(__name__)


def fetch_candidate_chunks(
    rulebook_id: str, query_vector: list[float], limit: int = 15
) -> list[dict]:
    """
    Executes a vector search against MongoDB Atlas to find the most relevant rulebook chunks.
    Filters by rulebookId and drops the raw vector from the response to preserve RAM.
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
                    "filter": {"rulebookId": ObjectId(rulebook_id)},
                }
            },
            {
                "$project": {
                    "_id": 0,
                    "chunkId": {"$toString": "$_id"},
                    "content": 1,
                    "index": 1,
                    "charCount": 1,
                    "score": {"$meta": "vectorSearchScore"},
                }
            },
        ]

        db = mongo_service.get_db()
        collection = db["RULEBOOK_TEXT"]

        results = list(collection.aggregate(pipeline))

        logger.info(
            "Successfully retrieved %d candidate chunks for rulebook %s",
            len(results),
            sanitise_log_input(rulebook_id),
        )
        return results
    except Exception:
        logger.exception(
            "Failed to execute vector search for rulebook %s",
            sanitise_log_input(rulebook_id),
        )
        raise
