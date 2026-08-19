import logging

import numpy as np
from fastapi import HTTPException

from app.retrieval.reranker import rerank_chunks
from app.retrieval.vector_store import fetch_candidate_chunks

logger = logging.getLogger(__name__)


def retrieve_context(query: str, rulebook_id: str, ml_models: dict) -> list[dict]:
    """
    Orchestrates the three-stage retrieval pipeline.
    1. Vectorises the query (Nomic 256d)
    2. Fetches top 15 candidates from MongoDB Vector Search.
    3. Re-ranks candidates down to the top 3 using the cross-encoder
    """
    try:
        # ========== Stage 1: Query Vectorisation ==========
        embedding_model = ml_models.get("embedding_model")
        if not embedding_model:
            raise ValueError("Embedding model missing from application state.")

        # Nomic v1.5 requires the 'search_query: ' prefix for user questions
        prefixed_query = f"search_query: {query}"

        query_embedding = embedding_model.encode(
            [prefixed_query], normalize_embeddings=True
        )

        truncated_query = query_embedding[:, :256]

        norms = np.linalg.norm(truncated_query, axis=1, keepdims=True)
        norms = np.maximum(norms, 1e-10)
        truncated_query = truncated_query / norms

        query_vector = truncated_query[0].tolist()

        # ========== Stage 2: Vector Search Retrieval ==========
        candidates = fetch_candidate_chunks(rulebook_id, query_vector, limit=15)

        if not candidates:
            logger.info("No candidates found in MongoDB for rulebook %s.", rulebook_id)
            return []

        # ========== Stage 3: Cross-Encoder Re-Ranking ==========
        reranker_model = ml_models.get("reranker_model")
        if not reranker_model:
            raise ValueError("Re-ranker model missing from application state.")

        top_chunks = rerank_chunks(query, candidates, reranker_model, top_k=3)

        logger.info(
            "Successfully retrieved and re-ranked top %d chunks.", len(top_chunks)
        )
        return top_chunks
    except Exception:
        logger.exception("Context retrieval pipeline failed.")
        raise HTTPException(
            status_code=500,
            detail="Internal error occurred while retrieving rulebook context.",
        )
