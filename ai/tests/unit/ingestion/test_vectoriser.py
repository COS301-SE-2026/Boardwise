from unittest.mock import MagicMock, patch

from app.ingestion.vectoriser import vectorise_chunks


def test_vectorise_chunks_standard_chunk_returns_success(
    valid_chunk_list, mock_nomic_embedder
):
    # Arrange
    expected_texts = [
        f"search_document: {chunk['content']}" for chunk in valid_chunk_list
    ]

    # Act
    success, chunks, reason = vectorise_chunks(valid_chunk_list, mock_nomic_embedder)

    # Assert
    assert success is True
    assert reason == ""
    assert len(chunks[0]["embedding"]) == 256

    mock_nomic_embedder.encode.assert_called_once_with(
        expected_texts, normalize_embeddings=True, convert_to_numpy=True
    )
