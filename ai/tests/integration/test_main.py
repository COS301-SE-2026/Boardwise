from unittest.mock import patch

import pytest
from app.main import app
from fastapi.testclient import TestClient


def test_lifespan_boot_populates_state_and_health_returns_200(client):
    # Arrange
    expected_health_response = {"status": "healthy", "service": "ai-gateway"}

    # Act
    response = client.get("/health")

    # Assert
    assert response.status_code == 200
    assert response.json() == expected_health_response

    assert hasattr(app.state, "ml_models") is True
    assert "embedding_model" in app.state.ml_models
    assert "reranker_model" in app.state.ml_models


@patch("app.main.mongo_service.ping_database")
def test_lifespan_infrastructure_failure_raises_fatal_exception(mock_mongo_ping):
    # Arrange
    mock_mongo_ping.side_effect = Exception("MongoDB connection timeout")

    # Act & Assert
    with pytest.raises(Exception, match="MongoDB connection timeout"), TestClient(app):
        pass
