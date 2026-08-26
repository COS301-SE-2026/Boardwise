from app.retrieval.reranker import rerank_chunks


def test_rerank_chunks_valid_candiates_returns_sorted_top_k(mock_reranker):
    # Arrange
    query = "How many cards do I draw?"
    candidates = [
        {"chunkId": "1", "content": "You draw two cards per turn."},
        {"chunkId": "2", "content": "Some other card rules"},
        {"chunkId": "3", "content": "Irrelevant movement rules."},
    ]

    expected_pairs = [
        [query, candidates[0]["content"]],
        [query, candidates[1]["content"]],
        [query, candidates[2]["content"]],
    ]

    # Act
    results = rerank_chunks(query, candidates, mock_reranker, top_k=2)

    # Assert
    assert len(results) == 2

    assert results[0]["chunkId"] == "1"
    assert results[0]["relevanceScore"] == 0.95
    assert results[1]["chunkId"] == "2"
    assert results[1]["relevanceScore"] == 0.80

    mock_reranker.predict.assert_called_once_with(expected_pairs)
