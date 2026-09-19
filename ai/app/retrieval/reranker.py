
import logging

from fastapi import HTTPException
from sentence_transformers import CrossEncoder

logger = logging.getLogger(__name__)

HYBRID_RANK_K = 60
CE_RANK_K = 60
CROSS_ENCODER_WEIGHT = 0.65

def rerank_chunks(query: str, candidate_chunks: list[dict], model: CrossEncoder, top_k: int = 3) -> list[dict]:
    """
    Evaluates candidate chunks against the user query using a cross-encoder model.
    Returns the top_k most relevant chunks to maintain a strict context window.
    """
    try:
        if not candidate_chunks:
            return []
        
        query_document_pairs = [[query, chunk["content"]] for chunk in candidate_chunks]
        
        scores = model.predict(query_document_pairs)
        
        for idx, chunk in enumerate(candidate_chunks):
            chunk["raw_ce_score"] = float(scores[idx])
        
        sorted_by_ce = sorted(candidate_chunks, key=lambda x: x["raw_ce_score"], reverse=True)
        ce_ranks = {chunk["chunkId"]: rank for rank, chunk in enumerate(sorted_by_ce, start=1)}
        
        for incoming_rank, chunk in enumerate(candidate_chunks, start=1):
            chunk_id = chunk["chunkId"]
            ce_rank = ce_ranks[chunk_id]
            
            rr_hybrid = 1.0 / (HYBRID_RANK_K + incoming_rank)
            rr_ce = 1.0 / (CE_RANK_K + ce_rank)
            
            final_score = (1.0 - CROSS_ENCODER_WEIGHT) * rr_hybrid + CROSS_ENCODER_WEIGHT * rr_ce
            
            chunk["relevanceScore"] = float(final_score)
        
        reranked_chunks = sorted(candidate_chunks, key=lambda x: x["relevanceScore"], reverse=True)
        
        logger.info("Successfully re-ranked and fused candidate chunks for the query.")
        
        return reranked_chunks[:top_k]
    except Exception:
        logger.exception("Failed to execute cross-encoder re-ranking.")
        raise HTTPException(
            status_code=500,
            detail="Internal error occurred while scoring document relevance."
        )
    