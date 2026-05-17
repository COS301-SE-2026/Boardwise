from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
# import jwt
from typing import Optional

from app.config import settings

bearer_scheme = HTTPBearer()

# def verify_jwt(
#     credentials: HTTPAuthorizationCredentials = Depends(bearer_scheme)
# ) -> dict:
#     token = credentials.credentials
#     try:
#         payload = jwt.decode(
#             token,
#             settings.JWT_SECRET,
#             algorithms=[settings.JWT_ALGORITHM]
#         )
#         return payload
#     except jwt.ExpiredSignatureError:
#         raise HTTPException(
#             status_code=status.HTTP_401_UNAUTHORIZED,
#             detail="Token has expired"
#         )
#     except jwt.InvalidTokenError:
#         raise HTTPException(
#             status_code=status.HTTP_401_UNAUTHORIZED,
#             detail="Invalid token"
#         )

def verify_jwt(credentials: Optional[HTTPAuthorizationCredentials] = Depends(HTTPBearer(auto_error=False))) -> dict:
    return {"userId": "507f1f77bcf86cd799439011"}