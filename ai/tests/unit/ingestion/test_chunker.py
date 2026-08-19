from ai.app.ingestion.chunker import generate_chunks
from bson.objectid import ObjectId

def test_generate_chunks_accepts_and_processes_standard_document_correctly(standard_extracted_text):
    """Injects standard extracted text from conftest"""
    # Act
    success, chunks, failure_reason = generate_chunks(standard_extracted_text)

    # Assert
    assert success is True
    assert failure_reason == ""

    assert len(chunks) == 3

    assert ObjectId.is_valid(chunks[0]["chunkId"]) is True
    assert chunks[0]["index"] == 0
    assert "takes a player board" in chunks[0]["content"]

def test_generate_chunks_accepts_text_with_whitespace_and_empty_chunks(messy_extracted_text):
    """Injects messy extracted text from conftest"""
    # Act
    success, chunks, failure_reason = generate_chunks(messy_extracted_text)

    # Assert
    assert success is True
    assert len(chunks) == 2
    assert chunks[0]["content"].startswith("Setup: Start") is True
    assert failure_reason == ""

def test_generate_chunks_fails_for_zero_valid_segments(empty_extracted_text):
    """Injects empty extracted text from conftest"""
    # Act
    success, chunks, failure_reason = generate_chunks(empty_extracted_text)

    # Assert
    assert success is False
    assert len(chunks) == 0
    assert failure_reason == "Chunks resulted in 0 valid segments"
