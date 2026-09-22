import logging

from app.config import settings
from app.services import lancedb_service
from app.utils.logging_utils import sanitise_log_input

logger = logging.getLogger(__name__)


def fetch_candidate_chunks(
    rulebook_id: str, query_text: str, query_vector: list[float], limit: int = 50
) -> list[dict]:
    """
    Executes a hybrid search (Vector + FTS) againts LanceDB.
    Fuses results using Reciprocal Rank Fusion (RFF), and penalises
    penalises low-confidence, decorative, or needs-review chunks.
    """
    try:
        candidates = max(getattr(settings, "LANCEDB_CANDIDATES", 60), limit * 2)

        vector_results = lancedb_service.query_vector(
            rulebook_id, query_vector, candidates
        )
        fts_results = lancedb_service.query_fts(rulebook_id, query_text, candidates)

        k = 60
        rrf_scores = {}
        chunk_map = {}

        def process_leg(results):
            for rank, chunk in enumerate(results, start=1):
                chunk_id = chunk["chunkId"]

                if chunk_id not in chunk_map:
                    chunk_map[chunk_id] = chunk
                    rrf_scores[chunk_id] = 0.0

                rrf_scores[chunk_id] += 1.0 / (k + rank)

        process_leg(vector_results)
        process_leg(fts_results)

        fused_candidates = []
        for chunk_id, rrf_score in rrf_scores.items():
            chunk = chunk_map[chunk_id]
            confidence = float(chunk.get("confidence", 1.0))
            
            penalty_multiplier = 1.0
            if chunk.get("type") == "decorative":
                penalty_multiplier *= 0.5
            if chunk.get("needsReview", False):
                penalty_multiplier *= 0.8

            hybrid_score = rrf_score * confidence * penalty_multiplier
            chunk["hybridRankScore"] = hybrid_score

            fused_candidates.append(chunk)

        fused_candidates.sort(key=lambda x: x["hybridRankScore"], reverse=True)
        top_candidates = fused_candidates[:limit]

        logger.info(
            "Successfully retrieved %d fused candidate chunks for rulebook %s",
            len(top_candidates),
            sanitise_log_input(rulebook_id),
        )
        return top_candidates
    except Exception:
        logger.exception(
            "Failed to execute vector search for rulebook %s",
            sanitise_log_input(rulebook_id),
        )
        raise
