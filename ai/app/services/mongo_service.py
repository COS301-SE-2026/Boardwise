import os
from datetime import datetime, timezone
from typing import Any
from pymongo import MongoClient
from pymongo.errors import ConnectionFailure
from bson import ObjectId

from app.config import settings

client = MongoClient(settings.MONGODB_URL)

def get_db():
    """Returns instance of the database"""
    db_name = os.getenv("DB_NAME") or settings.MONGODB_DATABASE or "ci_fallback_db"
    return client[db_name]

def create_rulebook(
    title: str,
    edition: str | None,
    contributor_id: str,
    language: str,
    r2_pdf_key: str
) -> str:
    """Inserts a new document into the RULEBOOK collection"""
    db = get_db()

    boardgame = db["BOARD_GAME"].find_one({"title": title})
    if not boardgame:
        raise ValueError(f"Boardgame '{title}' not found.")

    user = db["USER"].find_one({"_id": ObjectId(contributor_id)})
    if not user:
        raise ValueError(f"User '{contributor_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["RULEBOOK"].insert_one({
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
        "minPlayers":boardgame.get("minPlayers", -1),
        "maxPlayers":boardgame.get("maxPlayers", -1),
        "minAge":boardgame.get("minAge", -1),
        "duration":boardgame.get("duration", -1),
        "genres":boardgame.get("genres", []),
    })

    return str(result.inserted_id)

def update_rulebook_status(rulebook_id: str, status: str, version: int = -1) -> bool:
    """Updates the status of the specific rulebook"""
    db = get_db()

    filter_by_id = {"_id": ObjectId(rulebook_id)}
    update: dict[str, dict[str, Any]] = {"$set": {"status": status}}

    if version != -1:
        update["$set"]["version"] = version

    result = db["RULEBOOK"].update_one(filter_by_id, update)

    return result.modified_count == 1

def update_rulebook_r2_pdf_key(rulebook_id: str, r2_pdf_key: str) -> bool:
    """Updates the R2 PDF key of the specific rulebook"""
    db = get_db()

    result = db["RULEBOOK"].update_one(
        {"_id": ObjectId(rulebook_id)},
        {"$set": {"r2PdfKey": r2_pdf_key}}
    )

    return result.modified_count == 1

def create_ingestion_job(rulebook_id: str) -> str:
    """Inserts a new document into the INGESTION_JOB collection"""
    db = get_db()
    rulebook_obj_id = ObjectId(rulebook_id)

    rulebook = db["RULEBOOK"].find_one({"_id": rulebook_obj_id})
    if not rulebook:
        raise ValueError(f"Rulebook '{rulebook_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["INGESTION_JOB"].insert_one({
        "rulebookId": rulebook_obj_id,
        "stage": "Sanitise",
        "jobStatus": "Processing",
        "failureReason": None,
        "startedAt": now,
        "completedAt": None,
    })

    return str(result.inserted_id)

def update_ingestion_job(
    job_id: str,
    stage: str,
    job_status: str, # Processing | Completed | Failed
    failure_reason: str = ""
) -> bool:
    """Updates the specified Ingestion Job document"""
    db = get_db()

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

    result = db["INGESTION_JOB"].update_one(filter_by_id, update)

    return result.modified_count == 1

def create_rulebook_text(
    rulebook_id: str,
    chunks_list: list[dict]
) -> str:
    """Creates the RULEBOOK_TEXT document containing the array of embedded CHUNK subdocuments"""
    db = get_db()
    rulebook_obj_id = ObjectId(rulebook_id)

    rulebook = db["RULEBOOK"].find_one({"_id": rulebook_obj_id})
    if not rulebook:
        raise ValueError(f"Rulebook '{rulebook_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["RULEBOOK_TEXT"].insert_one({
        "rulebookId": rulebook_obj_id,
        "version": 0,
        "chunks": chunks_list,
        "updatedAt": now
    })

    return str(result.inserted_id)

def is_token_valid(jti: str) -> bool:
    """Checks the MongoDB database to see if the token has been invalidated"""
    db = get_db()

    doc = db["TOKEN_BLACKLIST"].find_one({"_id": jti})

    return doc is None

def get_ingestion_job(job_id: str) -> dict | None:
    """Returns a single INGESTION_JOB document with the matching id"""
    db = get_db()

    doc = db["INGESTION_JOB"].find_one({"_id": ObjectId(job_id)})

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
