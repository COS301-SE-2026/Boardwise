import boto3
from botocore.exceptions import ClientError

from app.config import settings

s3_client = boto3.client(
    "s3",
    endpoint_url=settings.R2_ENDPOINT_URL,
    aws_access_key_id=settings.R2_ACCESS_KEY,
    aws_secret_access_key=settings.R2_SECRET_KEY,
    region_name="auto"
)


def upload_pdf(file_bytes: bytes, r2_key: str) -> str:
    try:
        s3_client.put_object(
            Bucket=settings.R2_BUCKET_RULEBOOKS,
            Key=r2_key,
            Body=file_bytes,
            ContentType="application/pdf"
        )
        return r2_key
    except ClientError as e:
        raise RuntimeError(f"R2 upload failed: {str(e)}")
    
def generate_r2_key(rulebook_id: str, filename: str) -> str:
    safe_filename = filename.replace(" ", "_").lower()
    return f"rulebooks/{rulebook_id}/{safe_filename}"