from unittest.mock import MagicMock, patch

import numpy as np
from app.retrieval.retriever import retrieve_context


@patch("app.retrieval.retriever.fetch_candidate_chunks")
@patch("app.retrieval.retriever.rerank_chunks")
def test_retrieve_context_valid_query_orchestrates_successfully(
    mock_rerank_chunks, mock_fetch_candidate_chunks
):
    # Arrange
    query = "How do I win?"
    rulebook_id = "rulebook_123"

    mock_embedder = MagicMock()
    mock_embedder.encode.return_value = np.array([[0.5] * 768])

    mock_reranker = MagicMock()

    ml_models = {"embedding_model": mock_embedder, "reranker_model": mock_reranker}

    fetched_candidates = [{"content": "candidate_1"}, {"content": "candidate_2"}]
    mock_fetch_candidate_chunks.return_value = fetched_candidates

    expected_top_chunks = [{"content": "candidate_1"}]
    mock_rerank_chunks.return_value = expected_top_chunks

    # Act
    result = retrieve_context(query, rulebook_id, ml_models)

    # Assert
    assert result == expected_top_chunks

    mock_embedder.encode.assert_called_once_with(
        [f"search_query: {query}"], normalize_embeddings=True
    )

    mock_fetch_candidate_chunks.assert_called_once()
    fetch_args, kwargs = mock_fetch_candidate_chunks.call_args

    assert fetch_args[0] == rulebook_id
    assert len(fetch_args[1]) == 256
    assert kwargs["limit"] == 15

    mock_rerank_chunks.assert_called_once_with(
        query, fetched_candidates, mock_reranker, top_k=3
    )
