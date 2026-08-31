from unittest.mock import patch

from bson import ObjectId


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
        "completedAt": "2026-10-27T10:05:00Z",
    }

    # Act
    response = client.get(f"/api/vault/jobs/{mock_job_id}")

    # Assert
    assert response.status_code == 200
    assert response.json()["id"] == mock_job_id

    mock_mongo.get_ingestion_job.assert_called_once_with(mock_job_id)


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
def test_get_job_status_fails_for_job_id_that_does_not_exist(
    mock_mongo, client, mock_auth
):
    """Proves that the ingestion job status cannot be fetech for a valid job id that does not exist."""
    # Arrange
    mock_job_id = str(ObjectId())
    mock_mongo.get_ingestion_job.return_value = None

    # Act
    response = client.get(f"/api/vault/jobs/{mock_job_id}")

    # Assert
    assert response.status_code == 404
    assert response.json()["detail"] == f"Ingestion job with id '{mock_job_id}' does not exist."

    mock_mongo.get_ingestion_job.assert_called_once_with(mock_job_id)
