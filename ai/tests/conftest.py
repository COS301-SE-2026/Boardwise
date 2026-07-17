import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.dependencies import verify_jwt

@pytest.fixture
def client():
    """Provides a TestClient instance for router tests."""
    return TestClient(app)

@pytest.fixture
def mock_auth():
    """Overrides the JWT dependency to simulate an authenticated user."""
    def override_verify_jwt():
        return {"sub": "609c12345678901234567890", "username": "TestUser"}

    app.dependency_overrides[verify_jwt] = override_verify_jwt
    yield

    app.dependency_overrides.clear()