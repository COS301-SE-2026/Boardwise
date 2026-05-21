# test_r2.py
import boto3
from botocore.exceptions import ClientError
import os
from dotenv import load_dotenv

load_dotenv("../.env")

account_id = os.getenv("R2_ACCOUNT_ID")
access_key = os.getenv("R2_ACCESS_KEY")
secret_key = os.getenv("R2_SECRET_KEY")
bucket_rulebooks = os.getenv("R2_BUCKET_RULEBOOKS")
bucket_profiles = os.getenv("R2_BUCKET_PROFILES")
bucket_listings = os.getenv("R2_BUCKET_LISTINGS")

s3 = boto3.client(
    "s3",
    endpoint_url=f"https://{account_id}.r2.cloudflarestorage.com",
    aws_access_key_id=access_key,
    aws_secret_access_key=secret_key,
    region_name="auto"
)

try:
    s3.head_bucket(Bucket=bucket_rulebooks)
    print(f"Connected to R2 bucket '{bucket_rulebooks}' successfully.")
except ClientError as e:
    print(f"Connection failed: {e}")