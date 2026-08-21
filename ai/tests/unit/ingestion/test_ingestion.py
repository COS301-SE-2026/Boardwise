from unittest.mock import patch

from app.ingestion.ingestion import run_ingestion_pipeline


@patch("app.ingestion.ingestion.mongo_service")
@patch("app.ingestion.ingestion.r2_service")
@patch("app.ingestion.ingestion.vectorise_chunks")
@patch("app.ingestion.ingestion.generate_chunks")
@patch("app.ingestion.ingestion.extract_text")
@patch("app.ingestion.ingestion.sanitise_pdf")
def test_run_ingestion_pipeline_happy_path_completes_successfully(
    mock_sanitise,
    mock_extract,
    mock_chunk,
    mock_vectorise,
    mock_r2_service,
    mock_mongo_service,
    safe_pdf_bytes,
    standard_extracted_text,
    valid_chunk_list,
    mock_nomic_embedder,
):
    # Arrange
    file_bytes = safe_pdf_bytes
    filename = "mock_rules.pdf"
    rulebook_id = "rulebook_123"
    job_id = "job_123"

    mock_sanitise.return_value = (True, "")
    mock_extract.return_value = (True, standard_extracted_text, "")
    mock_chunk.return_value = (True, valid_chunk_list, "")
    mock_vectorise.return_value = (True, valid_chunk_list, "")

    expected_pdf_key = f"{rulebook_id}/{filename}"
    mock_r2_service.generate_pdf_key.return_value = expected_pdf_key
    mock_r2_service.upload_to_r2.return_value = True

    # Act
    run_ingestion_pipeline(
        file_bytes=file_bytes,
        filename=filename,
        rulebook_id=rulebook_id,
        job_id=job_id,
        embedding_model=mock_nomic_embedder,
    )

    # Assert
    mock_sanitise.assert_called_once_with(file_bytes)
    mock_extract.assert_called_once_with(file_bytes)
    mock_chunk.assert_called_once_with(standard_extracted_text)
    mock_vectorise.assert_called_once_with(valid_chunk_list, mock_nomic_embedder)

    assert mock_mongo_service.update_ingestion_job.call_count == 4

    mock_r2_service.generate_pdf_key.assert_called_once_with(rulebook_id, filename)
    mock_r2_service.upload_to_r2.assert_called_once_with(
        file_bytes, expected_pdf_key, content_type="application/pdf"
    )

    assert valid_chunk_list[0]["rulebookId"] == rulebook_id
    assert "createdAt" in valid_chunk_list[0]
    assert "updatedAt" in valid_chunk_list[0]

    mock_mongo_service.finalise_rulebook_ingestion.assert_called_once_with(
        rulebook_id, job_id, expected_pdf_key, valid_chunk_list
    )
