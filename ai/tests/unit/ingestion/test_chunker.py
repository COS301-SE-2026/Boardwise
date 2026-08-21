from app.ingestion.chunker import generate_chunks
from bson.objectid import ObjectId


def test_generate_chunks_standard_text_returns_success(
    standard_extracted_text,
):
    """Injects standard extracted text from conftest"""
    # Act
    success, chunks, failure_reason = generate_chunks(standard_extracted_text)

    # Assert
    assert success is True
    assert failure_reason == ""

    assert isinstance(chunks[0]["chunkId"], ObjectId)
    assert chunks[0]["index"] == 0
    assert "takes a player board" in chunks[0]["content"]


def test_generate_chunks_messy_text_returns_stripped_chunks(
    messy_extracted_text,
):
    """Injects messy extracted text from conftest"""
    # Act
    success, chunks, failure_reason = generate_chunks(messy_extracted_text)

    # Assert
    assert success is True
    assert failure_reason == ""

    assert isinstance(chunks[0]["chunkId"], ObjectId)
    assert chunks[0]["content"].startswith("Setup: Start") is True


def test_generate_chunks_zero_valid_segments_returns_false(empty_extracted_text):
    """Injects empty extracted text from conftest"""
    # Act
    success, chunks, failure_reason = generate_chunks(empty_extracted_text)

    # Assert
    assert success is False
    assert len(chunks) == 0
    assert failure_reason == "Chunks resulted in 0 valid segments"
