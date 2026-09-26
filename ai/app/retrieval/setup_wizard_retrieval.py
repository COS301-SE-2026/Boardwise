import logging

import numpy as np

from app.config import settings
from app.retrieval.reranker import rerank_chunks
from app.retrieval.vector_store import fetch_candidate_chunks

logger = logging.getLogger(__name__)

SETUP_QUERIES = [
    "game setup before you begin",
    "how to set up the board or play area",
    "what each player receives or takes at the start",
    "number of players and setup differences",
]

def get_setup_chunks(rulebook_id: str, ml_models: dict, max_numChunks: int =12) ->list[dict]:
    embedder = ml_models["embedding_model"]
    reranker = ml_models["reranker_moel"]

    pool:dict[int, dict] = {}

    for query in SETUP_QUERIES:
        vec = embedder.encode([f"search_query: {query}"], normalize_embeddings =True)
        vec = vec[:,: settings.EMBEDDING_DIMENSIONS]
        vec = vec / np.maximum(np.linalg.norm(vec, axis=1, keepdims=True), 1e-10)
        candidates = fetch_candidate_chunks(rulebook_id,query,vec[0].tolist(),limit=55)
        for c in candidates:
            if c.get("type") != "decorative":
                pool[c["index"]] = c

        if not pool:
            logger.warning("No setup candidates found for rulebook %s", rulebook_id)
            return []
        
    rerank_query = "game setup: components, board layout, and what each player starts with"
    ranked = rerank_chunks(rerank_query, list(pool.values()), reranker, top_k=max_chunks)
    logger.info("Selected %d setup chunks for rulebook: %s]", len(ranked), rulebook_id)
    return sorted(ranked,key=lambda c: c["index"])