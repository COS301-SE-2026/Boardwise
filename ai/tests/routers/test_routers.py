import pytest
from unittest.mock import patch
from bson import ObjectId

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


    