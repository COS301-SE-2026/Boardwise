import os
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()
environment: str = os.getenv("APP_ENV", "dev")

BASE_DIR = Path(__file__).resolve().parent.parent
DEFAULT_LANCEDB_DIR = str(BASE_DIR/"data"/"lancedb")

class Settings:
    MONGODB_URL: str | None = (os.getenv("PROD_DB_URL") if environment == "prod" else os.getenv("DEV_DB_URL"))
    MONGODB_DATABASE: str | None = os.getenv("DB_NAME")
    JWT_SECRET: str | None = os.getenv("JWT_SECRET")
    JWT_ALGORITHM: str | None = os.getenv("JWT_ALGORITHM", "HS512")
    R2_ACCOUNT_ID: str | None = os.getenv("R2_ACCOUNT_ID")
    R2_BUCKET_RULEBOOKS: str | None = os.getenv("R2_BUCKET_RULEBOOKS")
    R2_RULEBOOKS_URL: str | None = os.getenv("R2_RULEBOOKS_PUBLIC_PROD_URL")
    R2_ACCESS_KEY: str | None = os.getenv("R2_ACCESS_KEY")
    R2_SECRET_KEY: str | None = os.getenv("R2_SECRET_KEY")
    SYSTEM_CONTRIBUTOR_ID: str| None = os.getenv("SYSTEM_CONTRIBUTOR_ID")
    R2_ENDPOINT_URL: str = (f"https://{os.getenv('R2_ACCOUNT_ID')}.r2.cloudflarestorage.com")
    MAX_FILE_SIZE_MB: int = 50
    HF_TOKEN: str | None = os.getenv("HF_TOKEN")
    INTERNAL_WEBHOOK_SECRET: str | None = os.getenv("INTERNAL_SECRET")
    CPU_CORES: int = int(os.getenv("CPU_CORES", "1"))
    LANCEDB_URI: str = os.getenv("LANCEDB_URI", DEFAULT_LANCEDB_DIR)
    LANCEDB_TABLE_NAME: str = os.getenv("LANCEDB_TABLE_NAME", "rulebook_text")
    EMBEDDING_DIMENSIONS: int = int(os.getenv("EMBEDDING_DIMENSIONS", "512"))
    LANCEDB_IVF_PARTITIONS: int = int(os.getenv("LANCEDB_IVF_PARTITIONS", "256"))
    LANCEDB_CANDIDATES: int = int(os.getenv("LANCEDB_CANDIDATES", "25"))
    UNSTRUCTURED_API_URL: str | None = (os.getenv("UNSTRUCTURED_PROD_URL") if environment == "prod" else os.getenv("UNSTRUCTURED_DEV_URL"))
    UNSTRUCTURED_API_KEY: str | None = os.getenv("UNSTRUCTURED_API_KEY")
    MIN_IMAGE_DIMENSION_PX: int = 60  # Used to flter out bullets, icons and decorative rules
    MIN_BOXED_REGION_AREA_PX: int = 4000  # Used to filter table-cell borders (small rules)
    MIN_BOXED_REGION_HEIGHT_PT: int = 20


settings = Settings()
