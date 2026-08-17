from unittest.mock import patch
import pytest
from bson import ObjectId
from app.main import app
from app.dependencies import verify_jwt

MINIMAL_PDF = (
    b"%PDF-1.4\n"
    b"1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
    b"2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
    b"2 0 obj\n<< /Type /Pages /Parent 2 0 R /MediaBox [0 0 612 792] >>\nendobj\n"
    b"xref\n0 3\n0000000000 65535 f \n0000000010 00000 n \n0000000060 00000 n \n0000000111 00000 n \n"
    b"trailer\n<< /Size 4 /Root 1 0 R >>\n"
    b"startxref\n110\n%%EOF\n"
)

def test_upload_rejects_non_pdf(client, mock_auth):
    """Proves the Gateway rejects invalid file types instantly."""
    # Arrange
    files = {"file": ("malware.sh", b"echo 'hacked'", "application/x-sh")}
    data = {"title": "Hack", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 415
    assert "Only PDF files" in response.json()["detail"]

@patch("app.routers.rulebook.mongo_service")
@patch("app.routers.rulebook.BackgroundTasks.add_task")
def test_upload_success_mocked(mock_add_task, mock_mongo, client, mock_auth):
    """Proves that a valid upload returns a 202 and starts the background job."""
    # Setup mocks
    mock_mongo.create_rulebook_and_job.return_value = ("mock_rulebook_123", "mock_job_123")

    # Arrange
    files = {"file": ("catan.pdf", MINIMAL_PDF, "application/pdf")}
    data = {"title": "Catan", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 202
    assert response.json()["rulebookId"] == "mock_rulebook_123"

    mock_mongo.create_rulebook_and_job.assert_called_once()
    mock_add_task.assert_called_once()

@patch("app.routers.job.mongo_service")
def test_get_job_status_success_for_valid_job_id(mock_mongo, client, mock_auth):
    """Proves that a valid job id returns the current status of an ingestion job"""
    # Arrange
    mock_job_id = str(ObjectId())
    mock_mongo.get_ingestion_job.return_value = {
        "job_id": mock_job_id,
        "rulebookId": "mock_rulebook_123",
        "stage": "Store",
        "jobStatus": "Completed",
        "failureReason": "",
        "startedAt": "2026-10-27T10:00:00Z",
        "completedAt": "2026-10-27T10:05:00Z"
    }

    # Act
    response = client.get(f"/api/vault/jobs/{mock_job_id}")

    # Assert
    assert response.status_code == 200
    assert response.json()["id"] == mock_job_id

    mock_mongo.get_ingestion_job.assert_called_once()

# ========== Edge Case Testing ==========
@patch("app.routers.rulebook.settings.MAX_FILE_SIZE_MB", 0)
def test_upload_rejects_pdf_too_large(client, mock_auth):
    """Proves that upload rejects files exceeding the size limit"""
    # Arrange
    tiny_pdf = b"%PDF-1.4\n"
    files = {"file": ("test.pdf", tiny_pdf, "application/pdf")}
    data = {"title": "Dune", "language":"en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 413
    assert "File exceeds 0MB limit" in response.json()["detail"]

def test_upload_rejects_non_pdf_bytes(client, mock_auth):
    """Proves that upload rejects files containing non-pdf bytes"""
    # Arrange
    non_pdf = b"%NotPDF-1.4\n"
    files = {"file": ("test.pdf", non_pdf, "application/pdf")}
    data = {"title": "Dune", "language":"en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 422
    assert "The uploaded file is empty or corrupted." in response.json()["detail"]

def test_upload_rejects_token_missing_sub_claim(client):
    """Proves that upload rejects files uploaded by contributors with a missing sub claim in their token"""
    # Arrange
    def override_verify_jwt_no_sub():
        return {
            "jti": "valid_token_id_123",
            "iat": 1700000000,
        }
    app.dependency_overrides[verify_jwt] = override_verify_jwt_no_sub

    try:
        dummy_file = {"file": ("test.pdf", b"%PDF-1.4\n", "application/pdf")}
        data = {"title": "Dune", "language": "en"}

        # Act
        response = client.post(
            "/api/vault/rulebooks/upload",
            data=data,
            files=dummy_file,
            headers={"Authorization": "Bearer fake_token"}
        )

        # Assert
        assert response.status_code == 401
        assert response.json()["detail"] == "sub is missing from token."
    finally:
        app.dependency_overrides.clear()

@patch("app.routers.rulebook.mongo_service")
def test_upload_throws_value_error(mock_mongo,client, mock_auth):
    """Proves that upload fails if a value related errror occurs"""
    # Arrange
    mock_mongo.create_rulebook_and_job.side_effect = ValueError("Boardgame 'Catan' not found.")

    # Arrange
    files = {"file": ("catan.pdf", MINIMAL_PDF, "application/pdf")}
    data = {"title": "Catan", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 400
    assert response.json()["detail"] == "Upload rejected"

    mock_mongo.create_rulebook_and_job.assert_called_once()

@patch("app.routers.rulebook.mongo_service")
def test_upload_throws_error_for_unexpected_failure(mock_mongo,client, mock_auth):
    """Proves that upload fails if an internal server errror occurs"""
    # Arrange
    mock_mongo.create_rulebook_and_job.side_effect = Exception("Unexpected error occurred.")

    # Arrange
    files = {"file": ("catan.pdf", MINIMAL_PDF, "application/pdf")}
    data = {"title": "Catan", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 500
    assert response.json()["detail"] == "An internal server error occurred while initialising the upload."

def test_get_job_status_fails_for_invalid_job_id(client, mock_auth):
    """Proves that the ingestion job status cannot be fetched if the job id is invalid"""
    # Arrange
    mock_job_id = "definitely_invalid"

    # Act
    response = client.get(f"/api/vault/jobs/{mock_job_id}")

    # Assert
    assert response.status_code == 400
    assert response.json()["detail"] == "Invalid job_id format."

@patch("app.routers.job.mongo_service")
def test_get_job_status_fails_for_job_id_that_does_not_exist(mock_mongo, client, mock_auth):
    """Proves that the ingestion job status cannot be fetech for a valid job id that does not exist."""
    # Arrange
    mock_job_id = str(ObjectId())
    mock_mongo.get_ingestion_job.return_value = None

    # Act
    response = client.get(f"/api/vault/jobs/{mock_job_id}")

    # Assert
    assert response.status_code == 404
    assert response.json()["detail"] == f"Ingestion job with id '{mock_job_id}' does not exist."

    mock_mongo.get_ingestion_job.assert_called_once()