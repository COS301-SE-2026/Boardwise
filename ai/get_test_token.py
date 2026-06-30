import jwt
from datetime import datetime, timedelta, timezone
from app.config import settings

payload = {
    "userId": "609c12345678901234567890",
    "jti": "fake-uuid-1234-5678",
    "exp": datetime.now(timezone.utc) + timedelta(days=1)
}

test_token = jwt.encode(payload, settings.JWT_SECRET, algorithm=settings.JWT_ALGORITHM)
print(f"\nTEST TOKEN:\n{test_token}\n")