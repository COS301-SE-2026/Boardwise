import logging

import numpy as np
from bson import ObjectId
from sentence_transformers import SentenceTransformer

from app.services import mongo_service
from app.utils.logging_utils import sanitise_log_input

logger = logging.getLogger(__name__)


def vectorise_chunks(
    chunks: list[dict], model: SentenceTransformer
) -> tuple[bool, list[dict], str]:
    """
    Vectorises chunk content using the Nomic embedding model.
    Truncates to 256 dimensions to prepare for MongoDB Binary Quantization.
    """
    try:
        if not chunks:
            return (False, [], "No chunks provided for vectorisation.")

        # Nomic v1.5 requires the 'search_document: ' prefix for documents stored in a DB
        texts = [f"search_document: {chunk['content']}" for chunk in chunks]

        embeddings = np.asarray(
            model.encode(texts, normalize_embeddings=True, convert_to_numpy=True)
        )

        truncated_embeddings = embeddings[:, :256]

        # Re-normalize after truncation to maintain cosine/hamming similarity accuracy
        norms = np.linalg.norm(truncated_embeddings, axis=1, keepdims=True)
        # Prevent division by zero
        norms = np.maximum(norms, 1e-10)
        truncated_embeddings = truncated_embeddings / norms

        for i, chunk in enumerate(chunks):
            # Convert the numpy array to standard Python list of floats for BSON serialization
            chunk["embedding"] = truncated_embeddings[i].tolist()

        logger.info("Successfully vectorised %d chunks.", len(chunks))
        return (True, chunks, "")
    except Exception:
        logger.exception("Vectorisation failed during Nomic model execution.")
        return (False, [], "Internal error occurred during vectorisation.")


async def background_vectorise_and_update(
    chunk_id: str, content: str, embedding_model: SentenceTransformer
):
    """
    Generates a 256d vector for the updated content.
    """
    try:
        payload = [{"content": content}]
        success, chunks, reason = vectorise_chunks(payload, embedding_model)

        if not success:
            logger.error(
                "Re-embedding failed for chunk %s: %s",
                sanitise_log_input(chunk_id),
                reason,
            )
            return

        final_embedding = chunks[0]["embedding"]

        db = mongo_service.get_db()
        result = db["RULEBOOK_TEXT"].update_one(
            {"_id": ObjectId(chunk_id)}, {"$set": {"embedding": final_embedding}}
        )

        if result.modified_count > 0:
            logger.info(
                "Successfully updated embedding for chunk %s",
                sanitise_log_input(chunk_id),
            )
        else:
            logger.warning(
                "Chunk %s was not found during re-embedding update",
                sanitise_log_input(chunk_id),
            )
    except Exception:
        logger.exception(
            "Unexpected error occurred while re-embedding chunk %s",
            sanitise_log_input(chunk_id),
        )
