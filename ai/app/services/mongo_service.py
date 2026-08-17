import os
import logging
from datetime import datetime, timezone, timedelta
from typing import Any
from pymongo import MongoClient
from pymongo.errors import ConnectionFailure
from bson import ObjectId

from app.config import settings

logger = logging.getLogger(__name__)

client = MongoClient(settings.MONGODB_URL)

# 50MB worst-case OCR estimate
STALE_JOB_THRESHOLD_MINUTES = 20

def get_db():
    """Returns instance of the database"""
    db_name = os.getenv("DB_NAME") or settings.MONGODB_DATABASE or "ci_fallback_db"
    return client[db_name]

def create_rulebook(
    title: str,
    edition: str | None,
    contributor_id: str,
    language: str,
    r2_pdf_key: str,
    session=None
) -> str:
    """Inserts a new document into the RULEBOOK collection"""
    db = get_db()

    boardgame = db["BOARD_GAME"].find_one({"title": title}, session=session)
    if not boardgame:
        logger.warning("Rulebook creation rejected: boardgame '%s' not found.", title)
        raise ValueError(f"Boardgame '{title}' not found.")

    user = db["USER"].find_one({"_id": ObjectId(contributor_id)}, session=session)
    if not user:
        logger.warning("Rulebook creation rejected: user '%s' not found.", contributor_id)
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
    }, session=session)

    return str(result.inserted_id)

def update_rulebook_status(rulebook_id: str, status: str, version: int = -1, session=None) -> None:
    """Updates the status of the specific rulebook"""
    db = get_db()

    filter_by_id = {"_id": ObjectId(rulebook_id)}
    update: dict[str, dict[str, Any]] = {"$set": {"status": status}}

    if version != -1:
        update["$set"]["version"] = version

    result = db["RULEBOOK"].update_one(filter_by_id, update, session=session)

    if result.modified_count != 1:
        logger.warning("Failed to update rulebook %s: no document matched.", rulebook_id)
        raise ValueError(f"Rulebook '{rulebook_id}' not found or not modified.")

def update_rulebook_r2_pdf_key(rulebook_id: str, r2_pdf_key: str, session=None) -> None:
    """Updates the R2 PDF key of the specific rulebook"""
    db = get_db()

    result = db["RULEBOOK"].update_one(
        {"_id": ObjectId(rulebook_id)},
        {"$set": {"r2PdfKey": r2_pdf_key}},
        session=session
    )

    if result.modified_count != 1:
        logger.warning("Failed to update rulebook %s: no document matched.", rulebook_id)
        raise ValueError(f"Rulebook '{rulebook_id}' not found or not modified.")

def create_ingestion_job(rulebook_id: str, session=None) -> str:
    """Inserts a new document into the INGESTION_JOB collection"""
    db = get_db()
    rulebook_obj_id = ObjectId(rulebook_id)

    rulebook = db["RULEBOOK"].find_one({"_id": rulebook_obj_id}, session=session)
    if not rulebook:
        logger.warning("Ingestion job creation rejected: rulebook '%s' not found.", rulebook_id)
        raise ValueError(f"Rulebook '{rulebook_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["INGESTION_JOB"].insert_one({
        "rulebookId": rulebook_obj_id,
        "stage": "Sanitise",
        "jobStatus": "Processing",
        "failureReason": None,
        "startedAt": now,
        "completedAt": None,
    }, session=session)

    return str(result.inserted_id)

def update_ingestion_job(
    job_id: str,
    stage: str,
    job_status: str, # Processing | Completed | Failed
    failure_reason: str = "", session=None) -> None:
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

    result = db["INGESTION_JOB"].update_one(filter_by_id, update, session=session)

    if result.modified_count != 1:
        logger.warning("Failed to update ingestion job %s: no document matched.", job_id)
        raise ValueError(f"Ingestion job '{job_id}' not found or not modified.")

def create_rulebook_text(
    rulebook_id: str,
    chunks_list: list[dict],
    session=None
) -> str:
    """Creates the RULEBOOK_TEXT document containing the array of embedded CHUNK subdocuments"""
    db = get_db()
    rulebook_obj_id = ObjectId(rulebook_id)

    rulebook = db["RULEBOOK"].find_one({"_id": rulebook_obj_id}, session=session)
    if not rulebook:
        logger.warning("Rulebook text creation rejected: rulebook '%s' not found.", rulebook_id)
        raise ValueError(f"Rulebook '{rulebook_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["RULEBOOK_TEXT"].insert_one({
        "rulebookId": rulebook_obj_id,
        "version": 0,
        "chunks": chunks_list,
        "updatedAt": now
    }, session=session)

    return str(result.inserted_id)

def is_token_valid(jti: str) -> bool:
    """Checks the MongoDB database to see if the token has been invalidated"""
    db = get_db()

    doc = db["TOKEN_BLACKLIST"].find_one({"_id": jti})

    return doc is None

def get_ingestion_job(job_id: str) -> dict | None:
    """
    Returns a single INGESTION_JOB document with the matching id.
    If the job is stuck in "Processing" past STALE_JOB_THRESHOLD_MINUTES,
    the method marks the job and its rulebook 'Failed' before returning
    """
    db = get_db()

    doc = db["INGESTION_JOB"].find_one({"_id": ObjectId(job_id)})

    if not doc:
        return None

    if doc["jobStatus"] == "Processing":
        age = datetime.now(timezone.utc) - doc["startedAt"].replace(tzinfo=timezone.utc)
        if age > timedelta(minutes=STALE_JOB_THRESHOLD_MINUTES):
            logger.warning("Job %s is stale (age %s) - marking as failed.", job_id, age)
            mark_pipeline_failed(
                str(doc["rulebookId"]),
                job_id,
                doc["stage"],
                reason=(f"Timed out after exceeding the {STALE_JOB_THRESHOLD_MINUTES}-minute processing threshold. Possible crash mid-pipeline.")
            )
            doc = db["INGESTION_JOB"].find_one({"_id": ObjectId(job_id)})

    doc["id"] = str(doc.pop("_id"))
    doc["rulebookId"] = str(doc["rulebookId"])

    return doc

def ping_database():
    """Pings the MongoDB database to check if it is available"""
    try:
        client.admin.command('ping')
    except ConnectionFailure as e:
        raise e

def create_rulebook_and_job(
    title: str,
    edition: str | None,
    contributor_id: str,
    language: str
) -> tuple[str, str]:
    """
    Atomically creates a RULEBOOK and its paired INGESTION_JOB.
    If either insert fails, both are rolled back.
    """
    with client.start_session() as session:
        with session.start_transaction():
            rulebook_id = create_rulebook(
                title=title,
                edition=edition,
                contributor_id=contributor_id,
                language=language,
                r2_pdf_key="",
                session=session
            )
            job_id = create_ingestion_job(rulebook_id, session=session)

    logger.info("Created rulebook %s with job %s.", rulebook_id, job_id)
    return (rulebook_id, job_id)

def finalise_rulebook_ingestion(
    rulebook_id: str,
    job_id: str,
    r2_pdf_key: str,
    chunks_list: list[dict]
) -> None:
    """
    Atomically applies all post-R2-upload Mongo writes: sets the rulebook's
    r2PdfKey, stores the text chunks, marks the rulebook as 'Ready', and marks
    the job as 'Completed'. Assumes that the R2 upload was successful.
    """
    with client.start_session() as session:
        with session.start_transaction():
            update_rulebook_r2_pdf_key(rulebook_id, r2_pdf_key, session=session)
            create_rulebook_text(rulebook_id, chunks_list, session=session)
            update_rulebook_status(rulebook_id, "Ready",1, session=session)
            update_ingestion_job(job_id, "Store", "Completed", session=session)

    logger.info("Finalised ingestion for rulebook %s.", rulebook_id)

def mark_pipeline_failed(rulebook_id: str, job_id: str, stage: str, reason: str) -> None:
    """Atomically marks both the ingestion job and its rulebook as Failed."""
    with client.start_session() as session:
        with session.start_transaction():
            update_ingestion_job(job_id, stage, "Failed", reason, session=session)
            update_rulebook_status(rulebook_id, "Failed", session=session)

    logger.info("Marked pipeline failed for rulebook %s at stage %s.", rulebook_id, stage)
