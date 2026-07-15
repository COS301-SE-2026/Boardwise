import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    MONGODB_URL: str | None = os.getenv("DB_URL")
    MONGODB_DATABASE: str | None = os.getenv("DB_NAME")
    JWT_SECRET: str | None = os.getenv("JWT_SECRET")
    JWT_ALGORITHM: str | None = os.getenv("JWT_ALGORITHM")
    R2_ACCOUNT_ID: str | None = os.getenv("R2_ACCOUNT_ID")
    R2_BUCKET_RULEBOOKS: str | None = os.getenv("R2_BUCKET_RULEBOOKS")
    R2_ACCESS_KEY: str | None = os.getenv("R2_ACCESS_KEY")
    R2_SECRET_KEY: str | None = os.getenv("R2_SECRET_KEY")
    R2_ENDPOINT_URL: str = f"https://{os.getenv('R2_ACCOUNT_ID')}.r2.cloudflarestorage.com"
    MAX_FILE_SIZE_MB: int = 50

settings = Settings()