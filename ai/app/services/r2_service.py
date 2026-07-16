import os
import logging
import boto3
from botocore.exceptions import ClientError
from app.config import settings

logger = logging.getLogger(__name__)

s3 = boto3.client(
    service_name="s3",
    endpoint_url=settings.R2_ENDPOINT_URL,
    aws_access_key_id=settings.R2_ACCESS_KEY,
    aws_secret_access_key=settings.R2_SECRET_KEY,
    region_name="auto",
)

def upload_to_r2(file_bytes: bytes, r2_key: str, content_type: str) -> bool:
    """
    Uploads raw bytes to R2 bucket under the given key.
    Returns: True if upload was successful and False otherwise.
    """
    try:
        response = s3.put_object(
            Bucket=settings.R2_BUCKET_RULEBOOKS,
            Key=r2_key,
            Body=file_bytes,
            ContentType=content_type,
        )
        if response:
            response_object = {
                "status":response['ResponseMetadata']['HTTPStatusCode'],
                "etag":response['ETag'],
                "versionId": response.get('VersionId'),
            }
            logger.info("R2 Upload Success: %s", response_object)
        return True
    except Exception as e:
        logger.exception("Failed to upload to R2: %s", e)
        return False

def generate_pdf_key(rulebook_id: str, filename: str)->str:
    """Returns: rulebooks/{rulebook_id}/{safe_filename}.pdf"""
    name, _ = os.path.splitext(filename)
    safe_filename = name.strip().replace(" ", "_").lower()
    return f"rulebooks/{rulebook_id}/{safe_filename}.pdf"

def ping_r2_storage():
    """Pings the S3 compatible object storage"""
    try:
        s3.head_bucket(Bucket=settings.R2_BUCKET_RULEBOOKS)
    except ClientError as e:
        logger.exception("R2 Connection failed")
        raise e
