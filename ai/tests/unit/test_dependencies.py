from datetime import datetime, timedelta, timezone
from unittest.mock import MagicMock, patch

import jwt
import pytest
from app.config import settings
from app.dependencies import verify_jwt
from fastapi import HTTPException


# Helper to forge test tokens
def create_test_token(payload_overrides: dict | None = None) -> str:
    payload = {
        "jti": "test-uuid-123",
        "sub": "609c1234",
        "exp": datetime.now(timezone.utc) + timedelta(minutes=15),
    }
    if payload_overrides:
        payload.update(payload_overrides)
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)


@patch("app.services.mongo_service.is_token_valid")
def test_verify_jwt_valid_token_returns_payload(mock_is_valid):
    # Arrange
    mock_is_valid.return_value = True
    valid_token = create_test_token()
    mock_credentials = MagicMock(credentials=valid_token)

    # Act
    result = verify_jwt(mock_credentials)

    # Assert
    assert result["sub"] == "609c1234"
    assert result["jti"] == "test-uuid-123"

    mock_is_valid.assert_called_once_with("test-uuid-123")


def test_verify_jwt_missing_jti_raises_401():
    # Arrange
    bad_token = create_test_token({"jti": ""})
    mock_credentials = MagicMock(credentials=bad_token)

    # Act
    with pytest.raises(HTTPException) as exc_info:
        verify_jwt(mock_credentials)

    # Assert
    assert exc_info.value.status_code == 401
    assert "missing JTI" in exc_info.value.detail


@patch("app.services.mongo_service.is_token_valid")
def test_verify_jwt_blacklisted_token_raises_401(mock_is_valid):
    # Arrange
    mock_is_valid.return_value = False
    token = create_test_token()
    mock_credentials = MagicMock(credentials=token)

    # Act
    with pytest.raises(HTTPException) as exc_info:
        verify_jwt(mock_credentials)

    # Assert
    assert exc_info.value.status_code == 401
    assert "revoked" in exc_info.value.detail

    mock_is_valid.assert_called_once_with("test-uuid-123")
