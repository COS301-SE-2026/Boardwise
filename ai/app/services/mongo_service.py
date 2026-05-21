from datetime import datetime, timezone
from bson import ObjectId
from pymongo import MongoClient

from app.config import settings

client = MongoClient(settings.MONGODB_URI)
db = client[settings.MONGODB_DATABASE]

rulebook_collection     = db["RULEBOOK"]
rulebook_text_collection = db["RULEBOOK_TEXT"]
ingestion_job_collection = db["INGESTION_JOB"]


def create_rulebook_document(
    game_name: str,
    edition: str,
    contributor_id: str,
    game_id: str,
    r2_pdf_key: str
) -> str:
    now = datetime.now(timezone.utc)
    rulebook_id = ObjectId()

    rulebook_collection.insert_one({
        "_id": rulebook_id,
        "game_name": game_name,
        "edition": edition,
        "status": "Processing",
        "version": 0,
        "contributor_id": ObjectId(contributor_id),
        "game_id": ObjectId(game_id),
        "r2_pdf_key": r2_pdf_key,
        "uploaded_at": now,
        "updated_at": now
    })

    return str(rulebook_id)


def create_ingestion_job(rulebook_id: str) -> str:
    now = datetime.now(timezone.utc)
    job_id = ObjectId()

    ingestion_job_collection.insert_one({
        "_id": job_id,
        "rulebook_id": ObjectId(rulebook_id),
        "stage": "Sanitise",
        "job_status": "Processing",
        "failure_reason": None,
        "started_at": now,
        "completed_at": None
    })

    return str(job_id)


def update_ingestion_job(
    job_id: str,
    stage: str,
    job_status: str,                   # Processing | Completed | Failed
    failure_reason: str = None
):
    now = datetime.now(timezone.utc)
    update = {
        "$set": {
            "stage": stage,
            "job_status": job_status,
            "failure_reason": failure_reason,
            "completed_at": now if job_status in ["Completed", "Failed"] else None
        }
    }
    ingestion_job_collection.update_one(
        {"_id": ObjectId(job_id)},
        update
    )

def create_rulebook_text(rulebook_id: str, content: str):
    now = datetime.now(timezone.utc)
    rulebook_text_collection.insert_one({
        "_id": ObjectId(),
        "rulebook_id": ObjectId(rulebook_id),
        "content": content,
        "version": 1,
        "updated_at": now
    })


def update_rulebook_status(rulebook_id: str, status: str, version: int = None):
    now = datetime.now(timezone.utc)
    update_fields = {
        "status": status,
        "updated_at": now
    }
    if version is not None:
        update_fields["version"] = version

    rulebook_collection.update_one(
        {"_id": ObjectId(rulebook_id)},
        {"$set": update_fields}
    )

def update_rulebook_r2_key(rulebook_id: str, r2_key: str):
    rulebook_collection.update_one(
        {"_id": ObjectId(rulebook_id)},
        {"$set": {"r2_pdf_key": r2_key}}
    )

def is_token_blacklisted(jti: str) -> bool:
    """
    Synchronously checks the shared MongoDB database to see if the 
    Java backend has invalidated this token.
    """
    collection = db.get_collection("tokenBlackList") # Verify your exact collection name
    
    # Query for the document matching the jti
    result = collection.find_one({"_id": jti})
    
    return result is not None