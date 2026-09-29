import logging
from typing import Annotated

import jwt
from fastapi import Depends, HTTPException, Path, Security, status
from fastapi.security import APIKeyHeader, HTTPAuthorizationCredentials, HTTPBearer

from app.config import settings
from app.services import lancedb_service, mongo_service

bearer_scheme = HTTPBearer()

logger = logging.getLogger(__name__)


def verify_jwt(
    credentials: Annotated[HTTPAuthorizationCredentials, Depends(bearer_scheme)],
) -> dict:
    """
    Verifies if the JWT attached to bearer is valid.
    Returns the Payload if the JWT is valid.
    Raises an exception if it is not.
    """
    token = credentials.credentials
    try:
        payload = jwt.decode(
            token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM]
        )

        jti = payload.get("jti")
        if not jti:
            logger.warning("Token structure is invalid as the JTI is missing.")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token structure: missing JTI.",
            )

        if not mongo_service.is_token_valid(jti):
            logger.warning("Token is invalid because it has been blacklisted.")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token has been revoked.",
            )

        logger.info("JWT is valid")
        return payload

    except jwt.ExpiredSignatureError as e:
        logger.warning("Rejected request: Token has expired.")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Token has expired."
        ) from e
    except jwt.InvalidTokenError as e:
        logger.exception("Invalid token error")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token."
        ) from e


def verify_index_ready(rulebook_id: str = Path(...)):
    """
    FastAPI dependency to ensure the LanceDB vector index is queryable
    and that the requested rulebook has vectors available.
    Returns True if ready
    Raises a 503 Service Unavailable if not ready.
    """
    if not lancedb_service.is_index_ready():
        logger.warning("LanceDB index is not globally ready.")
        raise HTTPException(
            status_code=503,
            detail="The rulebook search index is currently syncing. Please try again in a few moments.",
        )

    if not lancedb_service.rulebook_has_vectors(rulebook_id):
        logger.warning("LanceDB vectors are missing for rulebook %s", rulebook_id)
        raise HTTPException(
            status_code=503,
            detail="The rulebook search data is currently syncing. Please try again in a few moments.",
        )

    return True


internal_key_header = APIKeyHeader(name="X-Internal-Token", auto_error=True)


def verify_internal_token(header_value: str = Security(internal_key_header)):
    """
    FastAPI dependency that verifies the internal webhook token attached to the request.
    Returns the token if it is valid
    Raises a 403 Forbidden if the token is invalid
    """
    if header_value != settings.INTERNAL_WEBHOOK_SECRET:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Invalid internal token",
        )
    return header_value
