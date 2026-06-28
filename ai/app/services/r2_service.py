import logging
import boto3
import os
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
            logger.info(f"R2 Upload Success: {response_object}")
        return True
    except Exception as e:
        logging.error(f"Failed to upload to R2: {str(e)}", exc_info=True)
        return False

def generate_pdf_key(rulebook_id: str, filename: str)->str:
    """Returns: rulebooks/{rulebook_id}/{safe_filename}.pdf"""
    name, _ = os.path.splitext(filename)
    safe_filename = name.replace(" ", "_").lower()
    return f"rulebooks/{rulebook_id}/{safe_filename}.pdf"

def generate_pdf_cover_key(rulebook_id: str)->str:
    """Returns: rulebooks/{rulebook_id}/cover.png"""
    return f"rulebooks/{rulebook_id}/cover.png"