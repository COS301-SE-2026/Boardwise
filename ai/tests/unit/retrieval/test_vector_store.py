from unittest.mock import MagicMock, patch

from app.retrieval.vector_store import fetch_candidate_chunks
from bson import ObjectId


@patch("app.retrieval.vector_store.mongo_service")
def test_fetch_candidate_chunks_valid_query_returns_results(mock_mongo_service):
    # Arrange
    rulebook_id = str(ObjectId())
    query_vector = [0.1] * 256
    expected_limit = 15

    mock_db = MagicMock()
    mock_collection = MagicMock()
    expected_results = [{"chunkId": "chunk_1", "content": "Board setup rules."}]

    mock_collection.aggregate.return_value = expected_results
    mock_db.__getitem__.return_value = mock_collection
    mock_mongo_service.client.get_default_database.return_value = mock_db

    # Act
    results = fetch_candidate_chunks(rulebook_id, query_vector, expected_limit)

    # Assert
    assert results == expected_results

    mock_collection.aggregate.assert_called_once()

    # Extract the pipeline argument passed to aggregate() to verify the query
    pipeline_arg = mock_collection.aggregate.call_args[0][0]

    assert "$vectorSearch" in pipeline_arg[0]
    assert pipeline_arg[0]["$vectorSearch"]["queryVector"] == query_vector
    assert pipeline_arg[0]["$vectorSearch"]["limit"] == expected_limit
    assert pipeline_arg[0]["$vectorSearch"]["filter"]["rulebookId"] == ObjectId(rulebook_id)

    assert "$project" in pipeline_arg[1]
