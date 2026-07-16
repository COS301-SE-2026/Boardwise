import os
from unittest.mock import patch, MagicMock
from datetime import datetime, timezone
from app.pipeline import ingestion
from app.services import mongo_service
from bson import ObjectId
from botocore.exceptions import ClientError

@patch("app.services.extractor.pytesseract.image_to_string")
@patch("app.services.extractor.Image.open")
@patch("app.services.r2_service.upload_to_r2")
def test_run_ingestion_pipeline_success(mock_upload_pdf, mock_image_open, mock_ocr, seed_board_game, seed_user, mock_pdf_bytes):
    """Verifies that the ingestion pipeline runs successfully when given the correct parameters"""
    # Arrange
    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "This text was successfully extracted via Tesseract OCR."
    mock_upload_pdf.return_value = "The file has been sent to the r2 bucket."

    rulebook_id = mongo_service.create_rulebook("Dune", None, seed_user, "en", "")
    job_id = mongo_service.create_ingestion_job(rulebook_id)
    filename = "Dune Rulebook.pdf"

    # Act
    ingestion.run_ingestion_pipeline(mock_pdf_bytes, filename, rulebook_id, job_id)

    # Assert
    ingestion_job = mongo_service.get_ingestion_job(job_id)
    assert ingestion_job is not None
    assert ingestion_job["jobStatus"] == "Completed"
    assert ingestion_job["stage"] == "Store"
    assert isinstance(ingestion_job["completedAt"], datetime)
    delta = datetime.now(timezone.utc) - ingestion_job["completedAt"].replace(tzinfo=timezone.utc)
    assert delta.total_seconds() < 10

    db = mongo_service.client[os.environ["DB_NAME"]]
    rulebook = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})
    assert rulebook is not None
    assert rulebook["status"] == "Ready"
    assert rulebook["r2PdfKey"] == f"rulebooks/{rulebook_id}/dune_rulebook.pdf"

    rulebook_text = db.RULEBOOK_TEXT.find_one({"rulebookId": ObjectId(rulebook_id)})
    assert rulebook_text is not None
    assert len(rulebook_text["chunks"]) > 0

    mock_upload_pdf.assert_called_once()

@patch("app.services.extractor.pytesseract.image_to_string")
@patch("app.services.extractor.Image.open")
@patch("app.services.r2_service.upload_to_r2")
@patch("app.services.extractor.fitz.open")
def test_run_ingestion_pipeline_success_using_ocr_fallback(mock_fitz_open, mock_upload_pdf, mock_image_open, mock_ocr, seed_board_game, seed_user, mock_pdf_bytes):
    """Verifies that the ingestion pipeline runs successfully when given the correct parameters"""
    # Arrange
    mock_page = MagicMock()
    mock_page.get_text.return_value = "Too short."

    mock_pixmap = MagicMock()
    mock_pixmap.tobytes.return_value = b"dummy_image_bytes"
    mock_page.get_pixmap.return_value = mock_pixmap

    mock_document = MagicMock()
    mock_document.__iter__.return_value = [mock_page]
    mock_document.__len__.return_value = 1

    mock_fitz_open.return_value.__enter__.return_value = mock_document
    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "This text was successfully recovered via OCR fallback."
    mock_upload_pdf.return_value = "The file has been sent to the r2 bucket."

    rulebook_id = mongo_service.create_rulebook("Dune", None, seed_user, "en", "")
    job_id = mongo_service.create_ingestion_job(rulebook_id)
    filename = "Dune Rulebook.pdf"

    # Act
    ingestion.run_ingestion_pipeline(mock_pdf_bytes, filename, rulebook_id, job_id)

    # Assert
    ingestion_job = mongo_service.get_ingestion_job(job_id)
    assert ingestion_job is not None
    assert ingestion_job["jobStatus"] == "Completed"
    assert ingestion_job["stage"] == "Store"
    assert isinstance(ingestion_job["completedAt"], datetime)
    delta = datetime.now(timezone.utc) - ingestion_job["completedAt"].replace(tzinfo=timezone.utc)
    assert delta.total_seconds() < 10

    db = mongo_service.client[os.environ["DB_NAME"]]
    rulebook = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})
    assert rulebook is not None
    assert rulebook["status"] == "Ready"
    assert rulebook["r2PdfKey"] == f"rulebooks/{rulebook_id}/dune_rulebook.pdf"

    rulebook_text = db.RULEBOOK_TEXT.find_one({"rulebookId": ObjectId(rulebook_id)})
    assert rulebook_text is not None
    assert "This text was successfully recovered via OCR fallback." in rulebook_text["chunks"][0]["content"]

    mock_upload_pdf.assert_called_once()
    mock_page.get_text.assert_called_once()
    mock_page.get_pixmap.assert_called_once_with(dpi=300)
    mock_ocr.assert_called_once()

@patch("app.services.extractor.pytesseract.image_to_string")
@patch("app.services.extractor.Image.open")
@patch("app.services.r2_service.upload_to_r2")
def test_run_ingestion_pipeline_failure(mock_upload_pdf, mock_image_open, mock_ocr, seed_board_game, seed_user, mock_pdf_bytes):
    """Verifies that the ingestion pipeline crashes exeptions and updates the database to a Failed state."""
    # Arrange
    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "This text was successfully extracted via Tesseract OCR."
    err_res = {"Error": {"Code": 500, "Message": "Internal Server Error"}}
    mock_upload_pdf.side_effect = ClientError(err_res, 'UploadPart')

    rulebook_id = mongo_service.create_rulebook("Dune", None, seed_user, "en", "")
    job_id = mongo_service.create_ingestion_job(rulebook_id)
    filename = "Dune Rulebook.pdf"

    # Act
    ingestion.run_ingestion_pipeline(mock_pdf_bytes, filename, rulebook_id, job_id)

    # Assert
    ingestion_job = mongo_service.get_ingestion_job(job_id)
    assert ingestion_job is not None
    assert ingestion_job["jobStatus"] == "Failed"
    assert ingestion_job["stage"] == "Unknown"

    db = mongo_service.client[os.environ["DB_NAME"]]
    rulebook = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})
    assert rulebook is not None
    assert rulebook["status"] == "Failed"

    mock_upload_pdf.assert_called_once()
