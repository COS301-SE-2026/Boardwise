import logging
import re

import numpy as np

from app.config import settings
from app.retrieval.reranker import rerank_chunks
from app.retrieval.vector_search import fetch_candidate_chunks

logger = logging.getLogger(__name__)

SETUP_QUERIES = [
    "game setup before you begin",
    "how to set up the board or play area",
    "what each player receives or takes at the start",
    "number of players and setup differences",
]

END_OF_GAME_PATTERNS = [
    r"\bend of the game",
    r"\bgame ends\b",
    r"\brichest player wins\b",
    r"\bscoring occurs\b",
    r"\bin case of a tie\b",
]

END_GAME_REGEX = re.compile("|".join(END_OF_GAME_PATTERNS), re.IGNORECASE)


def is_end_game_chunk(content: str) -> bool:
    return bool(END_GAME_REGEX.search(content))


def _embed_query(query: str, embedding_model) -> list[float]:
    vec = embedding_model.encode([f"search_query: {query}"], normalize_embeddings=True)
    vec = vec[:, : settings.EMBEDDING_DIMENSIONS]
    vec = vec / np.maximum(np.linalg.norm(vec, axis=1, keepdims=True), 1e-10)
    return vec[0].tolist()


def get_setup_chunks(
    rulebook_id: str, ml_models: dict, max_chunks: int = 12
) -> list[dict]:
    """
    Returns the chunks most likely to describe game setup, in reading order,
    with end-hame/ scoring content excluded. Each result: {"index", "content"}.
    Raises on infrastructure errors; caller must catch and mark the job failed.
    """

    embedder = ml_models["embedding_model"]
    reranker = ml_models["reranker_model"]

    pool: dict[int, dict] = {}

    for query in SETUP_QUERIES:
        vec = _embed_query(query, embedder)
        candidates = fetch_candidate_chunks(rulebook_id, query, vec, limit=25)
        for c in candidates:
            if c.get("type") != "decorative" and not is_end_game_chunk(
                c.get("content", "")
            ):
                pool[c["index"]] = c

        if not pool:
            logger.warning("No setup candidates found for rulebook %s", rulebook_id)
            return []

    rerank_query = (
        "game setup: components, board layout, and what each player starts with"
    )
    ranked = rerank_chunks(
        rerank_query, list(pool.values()), reranker, top_k=max_chunks
    )
    logger.info("Selected %d setup chunks for rulebook: %s]", len(ranked), rulebook_id)
    return sorted(ranked, key=lambda c: c["index"])
