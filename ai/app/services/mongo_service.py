from pymongo import MongoClient
from pymongo.errors import ConnectionFailure
from bson import ObjectId
from datetime import datetime, timezone
from typing import Any

from app.config import settings

client = MongoClient(settings.MONGODB_URL)
db_name = settings.MONGODB_DATABASE or "ci_fallback_db"
db = client[db_name]

rulebook_collection = db["RULEBOOK"]
rulebook_text_collection = db["RULEBOOK_TEXT"]
ingestion_job_collection = db["INGESTION_JOB"]
boardgame_collection = db["BOARD_GAME"]
user_collection = db["USER"]
token_blacklist_collection = db["TOKEN_BLACKLIST"]

def create_rulebook(
    title: str,
    edition: str | None,
    contributor_id: str,
    language: str,
    r2_pdf_key: str
) -> str:
    """Inserts a new document into the RULEBOOK collection"""
    now = datetime.now(timezone.utc)

    boardgame = boardgame_collection.find_one({"title": title})
    if not boardgame:
        raise ValueError(f"Boardgame '{title}' not found.")

    user = user_collection.find_one({"_id": ObjectId(contributor_id)})
    if not user:
        raise ValueError(f"User '{contributor_id}' not found.")
    
    rulebook_id = ObjectId()


    rulebook_collection.insert_one({
        "_id": rulebook_id,
        "coverUrl": boardgame["imageURL"] if boardgame["imageURL"] else "",
        "gameId": boardgame["_id"],
        "title": title,
        "edition": edition,
        "status": "Processing",
        "version": 0,
        "contributorId": ObjectId(contributor_id),
        "contributorUsername": user.get("username", "Unknown"),
        "description": boardgame.get("description", ""),
        "language": language,
        "r2PdfKey": r2_pdf_key,
        "r2CoverKey": "rulebooks/default_cover.png",
        "lockHeldBy": None,
        "lockExpiresAt": None,
        "undoStack": [],
        "redoStack": [],
        "uploadedAt":now,
        "updatedAt":now,
    })

    return str(rulebook_id)

def update_rulebook_status(rulebook_id: str, status: str, version: int = -1) -> str:
    """Updates the status of the specific rulebook"""

    filter_by_id = {"_id": ObjectId(rulebook_id)}
    update: dict[str, dict[str, Any]] = {"$set": {"status": status}}

    if version != -1:
        update["$set"]["version"] = version

    rulebook_collection.update_one(filter_by_id, update)

    return "Rulebook status update was successful."

def update_rulebook_r2_pdf_key(rulebook_id: str, r2_pdf_key: str) -> str:
    """Updates the R2 PDF key of the specific rulebook"""

    rulebook_collection.update_one(
        {"_id": ObjectId(rulebook_id)},
        {"$set": {"r2PdfKey": r2_pdf_key}}
    )

    return "Rulebook R2 PDF key update was successful."

def create_ingestion_job(rulebook_id: str) -> str:
    """Inserts a new document into the INGESTION_JOB collection"""
    now = datetime.now(timezone.utc)
    job_id = ObjectId()

    ingestion_job_collection.insert_one({
        "_id": job_id,
        "rulebookId": ObjectId(rulebook_id),
        "stage": "Sanitise",
        "jobStatus": "Processing",
        "failureReason": None,
        "startedAt": now,
        "completedAt": None,
    })

    return str(job_id)

def update_ingestion_job(
    job_id: str,
    stage: str,
    job_status: str, # Processing | Completed | Failed
    failure_reason: str = ""
) -> str:
    """Updates the specified Injestion Job document"""
    now = datetime.now(timezone.utc)
    filter_by_id = {"_id": ObjectId(job_id)}
    update = {
        "$set": {
            "stage": stage,
            "jobStatus": job_status,
            "failureReason": failure_reason,
            "completedAt": now if job_status in ["Completed", "Failed"] else None
        }
    }

    ingestion_job_collection.update_one(filter_by_id, update)

    return "Ingestion job update was successful."

def create_rulebook_text(
    rulebook_id: str,
    chunks_list: list[dict]
) -> str:
    """Creates the RULEBOOK_TEXT document containing the array of embedded CHUNK subdocuments"""

    now = datetime.now(timezone.utc)
    rulebook_text_id = ObjectId()

    rulebook_text_collection.insert_one({
        "_id": rulebook_text_id,
        "rulebookId": ObjectId(rulebook_id),
        "version": 0,
        "chunks": chunks_list,
        "updatedAt": now
    })

    return str(rulebook_text_id)

def is_token_valid(jti: str) -> bool:
    """Checks the MongoDB database to see if the token has been invalidated"""

    doc = token_blacklist_collection.find_one({"_id": jti})

    return doc is None

def get_ingestion_job(job_id: str) -> dict | None:
    """Returns a single INGESTION_JOB document with the matching id"""
    doc = ingestion_job_collection.find_one({"_id": ObjectId(job_id)})

    if doc:
        doc["job_id"] = str(doc.pop("_id")) # Effectively replacing the _id field with the job_id field
        doc["rulebookId"] = str(doc["rulebookId"])

    return doc

def ping_database():
    """Pings the MongoDB database to check if it is available"""
    try:
        client.admin.command('ping')
    except ConnectionFailure as e:
        raise e
