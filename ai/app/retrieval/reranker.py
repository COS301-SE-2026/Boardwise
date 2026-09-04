
import logging

from fastapi import HTTPException
from sentence_transformers import CrossEncoder

logger = logging.getLogger(__name__)

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
            chunk["relevanceScore"] = float(scores[idx])
        
        reranked_chunks = sorted(candidate_chunks, key=lambda x: x["relevanceScore"], reverse=True)
        
        logger.info("Successfully re-ranked candidate chunks for the query.")
        
        return reranked_chunks[:top_k]
    except Exception:
        logger.exception("Failed to execute cross-encoder re-ranking.")
        raise HTTPException(
            status_code=500,
            detail="Internal error occurred while scoring document relevance."
        )
    