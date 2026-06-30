import pytest
from unittest.mock import patch

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
    """Proves that a vlaid upload returns a 202 and starts the background job."""
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