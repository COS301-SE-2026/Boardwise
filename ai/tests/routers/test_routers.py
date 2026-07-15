from unittest.mock import patch
import pytest
from bson import ObjectId
from app.main import app
from app.dependencies import verify_jwt

def test_upload_rejects_non_pdf(client, mock_auth):
    """Proves the Gateway rejects invalid file types instantly."""

    # Simulate uploading a shell script
    files = {"file": ("malware.sh", b"echo 'hacked'", "application/x-sh")}
    data = {"title": "Hack", "language": "en"}

    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    assert response.status_code == 415
    assert "Only PDF files" in response.json()["detail"]

@patch("app.routers.rulebook.mongo_service")
@patch("app.routers.rulebook.BackgroundTasks.add_task")
def test_upload_success_mocked(mock_add_task, mock_mongo, client, mock_auth):
    """Proves that a valid upload returns a 202 and starts the background job."""
    # Setup mocks
    mock_mongo.create_rulebook.return_value = "mock_rulebook_123"
    mock_mongo.create_ingestion_job.return_value = "mock_job_123"

    # Arrange
    mock_pdf = b"%PDF-1.4...Mock content..."
    files = {"file": ("catan.pdf", mock_pdf, "application/pdf")}
    data = {"title": "Catan", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 202
    assert response.json()["rulebookId"] == "mock_rulebook_123"

    mock_mongo.create_rulebook.assert_called_once()
    mock_add_task.assert_called_once()

@patch("app.routers.rulebook.mongo_service")
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
    response = client.get(f"/api/vault/rulebooks/status/{mock_job_id}")

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
    mock_mongo.create_rulebook.side_effect = ValueError("Boardgame 'Catan' not found.")

    # Arrange
    mock_pdf = b"%PDF-1.4...Mock content..."
    files = {"file": ("catan.pdf", mock_pdf, "application/pdf")}
    data = {"title": "Catan", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 400
    assert response.json()["detail"] == "Upload rejected"

    mock_mongo.create_rulebook.assert_called_once()

@patch("app.routers.rulebook.mongo_service")
def test_upload_throws_error_for_unexpected_failure(mock_mongo,client, mock_auth):
    """Proves that upload fails if an internal server errror occurs"""
    # Arrange
    mock_mongo.create_rulebook.side_effect = Exception("Unexpected error occured.")

    # Arrange
    mock_pdf = b"%PDF-1.4...Mock content..."
    files = {"file": ("catan.pdf", mock_pdf, "application/pdf")}
    data = {"title": "Catan", "language": "en"}

    # Act
    response = client.post("/api/vault/rulebooks/upload", data=data, files=files)

    # Assert
    assert response.status_code == 500
    assert response.json()["detail"] == "An internal server error occured while initialising the upload."