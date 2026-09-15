import os

from dotenv import load_dotenv

load_dotenv()
environment: str = os.getenv("APP_ENV", "dev")


class Settings:
    MONGODB_URL: str | None = (os.getenv("PROD_DB_URL") if environment == "prod" else os.getenv("DEV_DB_URL"))
    MONGODB_DATABASE: str | None = os.getenv("DB_NAME")
    JWT_SECRET: str | None = os.getenv("JWT_SECRET")
    JWT_ALGORITHM: str | None = os.getenv("JWT_ALGORITHM", "HS512")
    R2_ACCOUNT_ID: str | None = os.getenv("R2_ACCOUNT_ID")
    R2_BUCKET_RULEBOOKS: str | None = os.getenv("R2_BUCKET_RULEBOOKS")
    R2_ACCESS_KEY: str | None = os.getenv("R2_ACCESS_KEY")
    R2_SECRET_KEY: str | None = os.getenv("R2_SECRET_KEY")
    R2_ENDPOINT_URL: str = (f"https://{os.getenv('R2_ACCOUNT_ID')}.r2.cloudflarestorage.com")
    MAX_FILE_SIZE_MB: int = 50
    HF_TOKEN: str | None = os.getenv("HF_TOKEN")
    INTERNAL_WEBHOOK_SECRET: str | None = os.getenv("INTERNAL_SECRET")
    CPU_CORES: int = int(os.getenv("CPU_CORES", "1"))
    LANCEDB_URI: str = os.getenv("LANCEDB_URI", "/data/lancedb")
    LANCEDB_TABLE_NAME: str = os.getenv("LANCEDB_TABLE_NAME", "rulebook_text")
    EMBEDDING_DIMENSIONS: int = int(os.getenv("EMBEDDING_DIMENSIONS", "512"))
    LANCEDB_IVF_PARTITIONS: int = int(os.getenv("LANCEDB_IVF_PARTITIONS", "256"))
    LANCEDB_CANDIDATES: int = int(os.getenv("LANCEDB_CANDIDATES", "25"))


settings = Settings()
