import logging
import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from app.config import settings
from app.services.mongo_service import is_token_valid

bearer_scheme = HTTPBearer()

logger = logging.getLogger(__name__)

def verify_jwt(
    credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme)
) -> dict:
    """
    Verifies if the JWT attached to bearer is valid.
    Returns the Payload if the JWT is valid.
    Raises an exception if it is not.
    """
    token = credentials.credentials
    try:
        # Get the JWT payload
        payload = jwt.decode(
            token,
            settings.JWT_SECRET,
            algorithms=[settings.JWT_ALGORITHM]
        )

        # Extract jti (Token ID)
        jti = payload.get("jti")
        if not jti:
            logger.warning("Token structure is invalid as the JTI is missing.")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid token structure: missing JTI."
            )

        # Check if token is blacklisted
        if not is_token_valid(jti):
            logger.warning("Token is invalid because it has been blacklisted.")
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Token has been revoked."
            )

        logger.info("JWT is valid")
        return payload

    except jwt.ExpiredSignatureError as e:
        logger.warning("Rejected request: Token has expired.")
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token has expired."
        ) from e
    except jwt.InvalidTokenError as e:
        logger.error("Invalid token error: %s", str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token."
        ) from e
