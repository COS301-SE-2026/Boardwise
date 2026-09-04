from unittest.mock import patch
from botocore.exceptions import ClientError
from app.services import r2_service
from bson import ObjectId

def test_generate_pdf_key_sanitizes_filename():
    """
    Verifies the key generator strips spaces,
    handles casing, and builds the correct path
    """
    # Arrange
    rulebook_id = str(ObjectId())
    bad_filename = "  My Catan Rulebook FINAL . PDF "

    # Act
    key = r2_service.generate_pdf_key(rulebook_id, bad_filename)
    
    # Assert
    assert key == f"rulebooks/{rulebook_id}/my_catan_rulebook_final.pdf"

@patch("app.services.r2_service.s3")
def test_upload_to_r2_success(mock_s3_client):
    """Verifies that the boto3 client is called with the correct parameters."""
    # Arrange
    mock_bytes = b"%PDF-1.4...mock...bytes"
    rulebook_id = "mock_id_123"
    filename = "dune.pdf"
    key = f"rulebooks/{rulebook_id}/{filename}"

    # Act
    uploaded = r2_service.upload_to_r2(mock_bytes, key, "application/pdf")

    # Assert
    assert uploaded is True

    mock_s3_client.put_object.assert_called_once()

    _ , called_kwargs = mock_s3_client.put_object.call_args

    assert called_kwargs["Bucket"] == r2_service.settings.R2_BUCKET_RULEBOOKS
    assert called_kwargs["Key"] == key
    assert called_kwargs["ContentType"] == "application/pdf"

@patch("app.services.r2_service.s3")
def test_upload_to_r2_failure(mock_s3_client):
    """Verifies that the service fails gracefully if the Cloudflare network drops."""
    # Arrange
    err_res = {"Error": {"Code": 500, "Message": "Internal Server Error"}}
    mock_s3_client.put_object.side_effect = ClientError(err_res, 'UploadPart')

    mock_bytes = b"mock"

    # Act
    uploaded = r2_service.upload_to_r2(mock_bytes, "mock_id", "application/pdf")

    # Assert
    assert uploaded is False
    mock_s3_client.put_object.assert_called_once()
