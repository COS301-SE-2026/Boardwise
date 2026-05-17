import pytest
from unittest.mock import patch, MagicMock
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
from io import BytesIO

from app.pipeline.ingestion import run_ingestion_pipeline
from unittest.mock import patch, MagicMock, ANY

from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
from io import BytesIO

def make_valid_pdf() -> bytes:
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.drawString(100, 750, "Safe content.")
    c.save()
    return buffer.getvalue()

def make_empty_pdf() -> bytes:
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.save()
    return buffer.getvalue()

def inject_keyword(pdf_bytes: bytes, keyword: str) -> bytes:
    return pdf_bytes + f"\n%% {keyword}\n".encode("latin-1")


def make_text_pdf(text: str) -> bytes:
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.drawString(100, 750, text)
    c.save()
    return buffer.getvalue()


RULEBOOK_ID = "507f1f77bcf86cd799439011"
JOB_ID = "507f1f77bcf86cd799439012"


@pytest.fixture
def mock_mongo():
    with patch("app.pipeline.ingestion.mongo_service") as mock:
        yield mock


@pytest.fixture
def mock_r2():
    with patch("app.pipeline.ingestion.upload_pdf") as mock_upload, \
         patch("app.pipeline.ingestion.generate_r2_key") as mock_key:
        mock_key.return_value = f"rulebooks/{RULEBOOK_ID}/test.pdf"
        mock_upload.return_value = f"rulebooks/{RULEBOOK_ID}/test.pdf"
        yield mock_upload, mock_key


# --- happy path ---

def test_pipeline_completes_successfully(mock_mongo, mock_r2):
    pdf = make_text_pdf("Dune is a board game of conquest.")

    run_ingestion_pipeline(pdf, "test.pdf", RULEBOOK_ID, JOB_ID)

    mock_upload, _ = mock_r2

    # verify all stages were called
    mock_mongo.update_ingestion_job.assert_any_call(JOB_ID, "Sanitise", "Processing")
    mock_mongo.update_ingestion_job.assert_any_call(JOB_ID, "Extract", "Processing")
    mock_mongo.update_ingestion_job.assert_any_call(JOB_ID, "Extract", "Completed")

    # verify storage calls
    mock_upload.assert_called_once()
    mock_mongo.create_rulebook_text.assert_called_once()
    mock_mongo.update_rulebook_r2_key.assert_called_once()

    # verify final status
    mock_mongo.update_rulebook_status.assert_called_with(RULEBOOK_ID, "Ready", version=1)


# --- sanitisation failure ---

def test_pipeline_fails_on_unsafe_pdf(mock_mongo, mock_r2):
    unsafe_pdf = inject_keyword(make_valid_pdf(), "/OpenAction")

    run_ingestion_pipeline(unsafe_pdf, "test.pdf", RULEBOOK_ID, JOB_ID)

    mock_upload, _ = mock_r2

    # verify pipeline stopped at sanitise
    mock_mongo.update_ingestion_job.assert_any_call(
        JOB_ID, "Sanitise", "Failed", ANY
    )
    mock_mongo.update_rulebook_status.assert_called_with(RULEBOOK_ID, "Failed")

    # verify nothing was stored
    mock_upload.assert_not_called()
    mock_mongo.create_rulebook_text.assert_not_called()


# --- extraction failure ---

def test_pipeline_fails_on_empty_pdf(mock_mongo, mock_r2):
    pdf = make_empty_pdf()

    run_ingestion_pipeline(pdf, "test.pdf", RULEBOOK_ID, JOB_ID)

    mock_upload, _ = mock_r2

    # verify pipeline stopped at extract
    mock_mongo.update_ingestion_job.assert_any_call(
        JOB_ID, "Extract", "Failed",
        "Text extraction failed - scanned or image-based PDF"
    )
    mock_mongo.update_rulebook_status.assert_called_with(RULEBOOK_ID, "Failed")

    # verify nothing was stored
    mock_upload.assert_not_called()
    mock_mongo.create_rulebook_text.assert_not_called()


# --- invalid file ---

def test_pipeline_fails_on_invalid_file(mock_mongo, mock_r2):
    run_ingestion_pipeline(b"not a pdf", "test.pdf", RULEBOOK_ID, JOB_ID)

    mock_upload, _ = mock_r2
    mock_upload.assert_not_called()
    mock_mongo.create_rulebook_text.assert_not_called()
    mock_mongo.update_rulebook_status.assert_called_with(RULEBOOK_ID, "Failed")