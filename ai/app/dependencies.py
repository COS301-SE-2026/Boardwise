import logging
from typing import Annotated

import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.config import settings
from app.services import mongo_service

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
            token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM or "HS512"]
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


def verify_index_ready():
    """
    FastAPI dependency to ensure the vector index is queryable.
    Returns True if ready, otherwise raises a 503 Service Unavailable.
    """
    db = mongo_service.client.get_default_database()
    collection = db["RULEBOOK_TEXT"]

    try:
        indices = list(collection.list_search_indexes())

        for index in indices:
            if index.get("name") == "vector_index":
                if index.get("status") == "READY" and index.get("queryable") is True:
                    return True

                logger.warning(
                    "Vector index is present but not ready: %s", index.get("status")
                )
                raise HTTPException(
                    status_code=503,
                    detail="The rulebook search index is currently syncing. Please try again in a few moments.",
                )

        logger.error("Vector index 'vector_index' does not exist.")
        raise HTTPException(
            status_code=500, detail="Search infrustructure is misconfigured."
        )
    except HTTPException:
        raise
    except Exception:
        logger.exception("Failed to verify index status.")
        raise HTTPException(
            status_code=500, detail="Database connection error while verifying index."
        )
