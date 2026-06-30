import pytest
import jwt
from datetime import datetime, timedelta, timezone
from fastapi import HTTPException

from app.dependencies import verify_jwt
from app.config import settings
from unittest.mock import patch, MagicMock

# Helper to forge test tokens
def create_test_token(payload_overrides: dict = None) -> str:
    payload = {
        "jti": "test-uuid-123",
        "userId": "609c1234",
        "exp": datetime.now(timezone.utc) + timedelta(minutes=15)
    }
    if payload_overrides:
        payload.update(payload_overrides)
    return jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)

@patch("app.dependencies.is_token_valid")
def test_verify_jwt_success(mock_is_valid):
    mock_is_valid.return_value = True

    valid_token = create_test_token()
    mock_credentials = MagicMock(credentials=valid_token)

    result = verify_jwt(mock_credentials)

    assert result["userId"] == "609c1234"
    assert result["jti"] == "test-uuid-123"

def test_verify_jwt_missing_jti():
    bad_token = create_test_token({"jti":""})
    mock_credentials = MagicMock(credentials=bad_token)

    with pytest.raises(HTTPException) as exc_info:
        verify_jwt(mock_credentials)

    assert exc_info.value.status_code == 401
    assert "missing JTI" in exc_info.value.detail

@patch("app.dependencies.is_token_valid")
def test_verify_jwt_blacklisted(mock_is_valid):
    mock_is_valid.return_value = False

    token = create_test_token()
    mock_credentials = MagicMock(credentials=token)

    with pytest.raises(HTTPException) as exc_info:
        verify_jwt(mock_credentials)

    assert exc_info.value.status_code == 401
    assert "revoked" in exc_info.value.detail