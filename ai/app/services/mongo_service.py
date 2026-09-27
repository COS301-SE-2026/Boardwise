import logging
import os
from datetime import datetime, timedelta, timezone
from typing import Any

from bson import ObjectId
from pymongo import MongoClient

# from pymongo.errors import DuplicateKeyError
from app.config import settings

logger = logging.getLogger(__name__)

client = MongoClient(settings.MONGODB_URL)

# 50MB worst-case OCR estimate
STALE_JOB_THRESHOLD_MINUTES = 20

_CHUNK_PASSTHROUGH_FIELDS = (
    "index",
    "content",
    "type",
    "sourcePages",
    "metadata",
    "needsReview",
    "confidence",
    "associatedImageUrls",
)

_CHUNK_FIELD_DEFAULTS = {
    "type": "text",
    "sourcePages": [],
    "metadata": {},
    "needsReview": False,
    "confidence": 1.0,
    "associatedImageUrls": [],
}


def _coerce_object_id(value) -> ObjectId:
    """Accept an ObjectId or a 24-character hex string. Raise ValueError otherwise"""
    if isinstance(value, ObjectId):
        return value
    return ObjectId(str(value))


def _chunk_to_document(chunk: dict, rulebook_oid: ObjectId, now: datetime) -> dict:
    """Map a chunk into a RULEBOOK_TEXT document"""
    chunk_id = chunk.get("chunkId")
    if chunk_id is None:
        raise ValueError("Chunk is missing 'chunkId'.")

    content = chunk.get("content", "")
    doc = {
        "_id": _coerce_object_id(chunk_id),
        "rulebookId": rulebook_oid,
        "charCount": len(content),
        "createdAt": chunk.get("createdAt") or now,
        "updatedAt": now,
    }

    for field in _CHUNK_PASSTHROUGH_FIELDS:
        if field in chunk and chunk[field] is not None:
            doc[field] = chunk[field]

    for field, default in _CHUNK_FIELD_DEFAULTS.items():
        doc.setdefault(field, default)

    return doc


def get_db():
    """Returns instance of the database"""
    db_name = os.getenv("DB_NAME") or settings.MONGODB_DATABASE or "ci_fallback_db"
    return client[db_name]


def sanitise_for_log(user_input: str) -> str:
    """Removes line breaks to prevent Log Injection (Log Forging)."""
    return str(user_input).replace("\n", "_").replace("\r", "_")


def create_rulebook(
    title: str,
    edition: str | None,
    contributor_id: str,
    language: str,
    r2_pdf_key: str,
    session=None,
) -> str:
    """Inserts a new document into the RULEBOOK collection"""
    db = get_db()

    safe_title = str(title)

    boardgame = db["BOARD_GAME"].find_one({"title": safe_title}, session=session)
    if not boardgame:
        sanitised_title = sanitise_for_log(safe_title)
        logger.warning(
            "Rulebook creation rejected: boardgame '%s' not found.", sanitised_title
        )
        raise ValueError(f"Boardgame '{safe_title}' not found.")

    user = db["USER"].find_one({"_id": ObjectId(contributor_id)}, session=session)
    if not user:
        logger.warning(
            "Rulebook creation rejected: user '%s' not found.", contributor_id
        )
        raise ValueError(f"User '{contributor_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["RULEBOOK"].insert_one(
        {
            "coverUrl": boardgame["imageURL"] if boardgame["imageURL"] else "",
            "gameId": boardgame["_id"],
            "title": safe_title,
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
            "uploadedAt": now,
            "updatedAt": now,
            "minPlayers": boardgame.get("minPlayers", -1),
            "maxPlayers": boardgame.get("maxPlayers", -1),
            "minAge": boardgame.get("minAge", -1),
            "duration": boardgame.get("duration", -1),
            "genres": boardgame.get("genres", []),
        },
        session=session,
    )

    return str(result.inserted_id)


def update_rulebook_status(
    rulebook_id: str, status: str, version: int = -1, session=None
) -> None:
    """Updates the status of the specific rulebook"""
    db = get_db()

    filter_by_id = {"_id": ObjectId(rulebook_id)}
    update: dict[str, dict[str, Any]] = {"$set": {"status": status}}

    if version != -1:
        update["$set"]["version"] = version

    result = db["RULEBOOK"].update_one(filter_by_id, update, session=session)

    if result.modified_count != 1:
        logger.warning(
            "Failed to update rulebook %s: no document matched.", rulebook_id
        )
        raise ValueError(f"Rulebook '{rulebook_id}' not found or not modified.")


def update_rulebook_r2_pdf_key(rulebook_id: str, r2_pdf_key: str, session=None) -> None:
    """Updates the R2 PDF key of the specific rulebook"""
    db = get_db()

    result = db["RULEBOOK"].update_one(
        {"_id": ObjectId(rulebook_id)},
        {"$set": {"r2PdfKey": r2_pdf_key}},
        session=session,
    )

    if result.modified_count != 1:
        logger.warning(
            "Failed to update rulebook %s: no document matched.", rulebook_id
        )
        raise ValueError(f"Rulebook '{rulebook_id}' not found or not modified.")


def create_ingestion_job(rulebook_id: str, session=None) -> str:
    """Inserts a new document into the INGESTION_JOB collection"""
    db = get_db()
    rulebook_obj_id = ObjectId(rulebook_id)

    rulebook = db["RULEBOOK"].find_one({"_id": rulebook_obj_id}, session=session)
    if not rulebook:
        logger.warning(
            "Ingestion job creation rejected: rulebook '%s' not found.", rulebook_id
        )
        raise ValueError(f"Rulebook '{rulebook_id}' not found.")

    now = datetime.now(timezone.utc)

    result = db["INGESTION_JOB"].insert_one(
        {
            "rulebookId": rulebook_obj_id,
            "stage": "Sanitise",
            "jobStatus": "Processing",
            "failureReason": None,
            "startedAt": now,
            "completedAt": None,
        },
        session=session,
    )

    return str(result.inserted_id)


def update_ingestion_job(
    job_id: str,
    stage: str,
    job_status: str,  # Processing | Completed | Failed
    failure_reason: str = "",
    session=None,
) -> None:
    """Updates the specified Ingestion Job document"""
    db = get_db()

    safe_job_id = str(job_id)

    now = datetime.now(timezone.utc)
    filter_by_id = {"_id": ObjectId(safe_job_id)}
    update = {
        "$set": {
            "stage": stage,
            "jobStatus": job_status,
            "failureReason": failure_reason,
            "completedAt": now if job_status in ["Completed", "Failed"] else None,
        }
    }

    result = db["INGESTION_JOB"].update_one(filter_by_id, update, session=session)

    if result.modified_count != 1:
        sanitised_job_id = sanitise_for_log(safe_job_id)
        logger.warning(
            "Failed to update ingestion job %s: no document matched.", sanitised_job_id
        )
        raise ValueError(f"Ingestion job '{safe_job_id}' not found or not modified.")


def create_rulebook_text(
    rulebook_id: str, chunks_list: list[dict], session=None
) -> list[str]:
    """Creates individual flattened RULEBOOK_TEXT chunk documents for vector search."""
    db = get_db()
    rulebook_obj_id = ObjectId(rulebook_id)

    rulebook = db["RULEBOOK"].find_one({"_id": rulebook_obj_id}, session=session)
    if not rulebook:
        logger.warning(
            "Rulebook text creation rejected: rulebook '%s' not found.", rulebook_id
        )
        raise ValueError(f"Rulebook '{rulebook_id}' not found.")

    now = datetime.now(timezone.utc)
    chunks_to_insert = []

    for chunk in chunks_list:
        in_chunk = _chunk_to_document(chunk, rulebook_obj_id, now)

        chunks_to_insert.append(in_chunk)

    result = db["RULEBOOK_TEXT"].insert_many(chunks_to_insert, session=session)

    return [str(inserted_id) for inserted_id in result.inserted_ids]


def get_rulebook_text_chunk(chunk_id: str) -> dict | None:
    """Fetches a single RULEBOOK_TEXT document"""
    db = get_db()
    doc = db["RULEBOOK_TEXT"].find_one({"_id": ObjectId(chunk_id)})

    if not doc:
        return None

    doc["chunkId"] = str(doc.pop("_id"))
    doc["rulebookId"] = str(doc["rulebookId"])
    return doc


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

    safe_job_id = str(job_id)

    doc = db["INGESTION_JOB"].find_one({"_id": ObjectId(safe_job_id)})

    if not doc:
        return None

    if doc["jobStatus"] == "Processing":
        age = datetime.now(timezone.utc) - doc["startedAt"].replace(tzinfo=timezone.utc)
        if age > timedelta(minutes=STALE_JOB_THRESHOLD_MINUTES):
            sanitised_job_id = sanitise_for_log(safe_job_id)
            logger.warning(
                "Job %s is stale (age %s) - marking as failed.", sanitised_job_id, age
            )
            mark_pipeline_failed(
                str(doc["rulebookId"]),
                safe_job_id,
                doc["stage"],
                reason=(
                    f"Timed out after exceeding the {STALE_JOB_THRESHOLD_MINUTES}-minute processing threshold. Possible crash mid-pipeline."
                ),
            )
            doc = db["INGESTION_JOB"].find_one({"_id": ObjectId(safe_job_id)})

            if not doc:
                return None

    doc["id"] = str(doc.pop("_id"))
    doc["rulebookId"] = str(doc["rulebookId"])

    return doc


def ping_database():
    """Pings the MongoDB database to check if it is available"""
    client.admin.command("ping")


def create_rulebook_and_job(
    title: str, edition: str | None, contributor_id: str, language: str
) -> tuple[str, str]:
    """
    Atomically creates a RULEBOOK and its paired INGESTION_JOB.
    If either insert fails, both are rolled back.
    """
    with client.start_session() as session, session.start_transaction():
        rulebook_id = create_rulebook(
            title=title,
            edition=edition,
            contributor_id=contributor_id,
            language=language,
            r2_pdf_key="",
            session=session,
        )
        job_id = create_ingestion_job(rulebook_id, session=session)

    logger.info("Created rulebook %s with job %s.", rulebook_id, job_id)
    return (rulebook_id, job_id)


def store_rulebook_text_and_pdf_key(
    rulebook_id: str, r2_pdf_key: str, chunks_list: list[dict]
) -> None:
    """
    Atomically applies the Mongo-only half of finalisation
    (r2Pdfkey and text chunks) without marking the rulebook
    'Ready' or the job 'Complete'
    """
    with client.start_session() as session, session.start_transaction():
        update_rulebook_r2_pdf_key(rulebook_id, r2_pdf_key, session=session)
        create_rulebook_text(rulebook_id, chunks_list, session=session)

    logger.info(
        "Stored rulebook text and PDF key for rulebook %s. Vectors will still undergo LanceDB write.",
        rulebook_id,
    )


def mark_rulebook_ready(rulebook_id: str, job_id: str) -> None:
    """
    Second half of finalisation: marks the rulebook 'Ready' and the job 'Completed'.
    Called only after a successful LanceDB vector write for the rulebook
    """
    with client.start_session() as session, session.start_transaction():
        update_rulebook_status(rulebook_id, "Ready", 1, session=session)
        update_ingestion_job(job_id, "Store", "Completed", session=session)

    logger.info("Marked rulebook %s Ready and job %s Completed.", rulebook_id, job_id)


def mark_rulebook_pending_review(rulebook_id: str, job_id: str, reason: str) -> None:
    """
    Marks the rulebook 'PendingReview' and the job 'Processing'.
    Called only if the rulebook fails the quality check
    """
    with client.start_session() as session, session.start_transaction():
        update_rulebook_status(rulebook_id, "PendingReview", 1, session=session)
        update_ingestion_job(job_id, "Store", "Processing", reason, session=session)

    logger.info(
        "Marked rulebook %s as 'PendingReview' and job %s as 'Processing'.",
        rulebook_id,
        job_id,
    )


def mark_pipeline_failed(
    rulebook_id: str, job_id: str, stage: str, reason: str
) -> None:
    """Atomically marks both the ingestion job and its rulebook as Failed."""
    with client.start_session() as session, session.start_transaction():
        update_ingestion_job(job_id, stage, "Failed", reason, session=session)
        update_rulebook_status(rulebook_id, "Failed", session=session)

    logger.info(
        "Marked pipeline failed for rulebook %s at stage %s.", rulebook_id, stage
    )


def replace_rulebook_text(rulebook_id: str, chunks: list[dict]) -> int:
    """
    Atomically replace all RULEBOOK_TEXT chunks for one rulebook.
    - Deletes every existing document for the rulebook.
    - Inserts the new document derived from 'chunks'
    Returns the number of documents written
    Raises on failure
    """
    rulebook_oid = _coerce_object_id(rulebook_id)
    now = datetime.now(timezone.utc)
    documents = [_chunk_to_document(c, rulebook_oid, now) for c in chunks]

    db = get_db()

    def _delete_then_insert(session=None) -> None:
        db["RULEBOOK_TEXT"].delete_many(
            {"rulebookId": rulebook_oid}, session=session
        )
        if documents:
            db["RULEBOOK_TEXT"].insert_many(documents, session=session)

    with client.start_session() as session, session.start_transaction():
        _delete_then_insert(session=session)

    logger.info(
        "Replaced RULEBOOK_TEXT for rulebook %s with %d chunks",
        rulebook_id,
        len(documents),
    )
    return len(documents)


# #Setup Wizard
# def setup_wizard_indexes() -> None:
#     """
#     Creates required indexes on the SETUP_WIZARD COLLECTION.
#     Call once at startup.
#     """
#     db =get_db()
#     db["SETUP_WIZARD"].create_index("rulebookId", unique = True)

# def get_setup_wizard_by_rulebookId(rulebook_id: str) -> dict | None:
#     """
#     Fetches the SETUP_WIZARD document for a given rulebook, if one exists.
#     """

#     db = get_db()
#     doc = db["SETUP_WIZARD"].findOne({"rulebookId": ObjectId(rulebook_id)})

#     if not doc:
#         return None

#     doc["id"] = str(doc.pop("_id"))
#     doc["rulebookId"] = str(doc["rulebook"])

#     return doc

# def create_setup_wizard(rulebook_id: str, session=None) -> str:
#     """
#     Inserts a new queued SETUP_WIZARD document for a rulebook, seeded with
#     minPlayers/maxPlayers pulled from the rulebook, and points
#     Rulebook.setupWizardId back at it. Raises ValueError if the rulebook
#     doesn't exist or already has a wizard.
#     """

#     db = get_db()
#     rulebook_object_id = ObjectId(rulebook_id)

#     rulebook = db["RULEBOOK"].find_one({"_id": rulebook_object_id}, session = session)
#     if not rulebook:
#         logger.warning("Setup wizard creation rejected: rulebook '%s' not found.", rulebook_id)
#         raise ValueError(f"Rulebook '{rulebook_id}' not found.")

#     now = datetime.now(timezone.utc)

#     result = db["SETUP_WIZARD"].insert_one(
#         {
#             "rulebookId": rulebook_obj_id,
#             "createdAt": now,
#             "updatedAt": now,
#             "schemaVersion": 1,
#             "job": {
#                 "status": "queued",
#                 "progress": 0,
#                 "generatedAt": None,
#                 "error": None,
#             },
#             "game": None,
#             "config": {
#                 "minPlayers": rulebook.get("minPlayers", -1),
#                 "maxPlayers": rulebook.get("maxPlayers", -1),
#             },
#             "summary": None,
#             "components": [],
#             "phases": [],
#             "warnings": [],
#         },
#         session  = session
#     )

#     wizard_id = str(result.inserted_id)

#     db["RULEBOOK"].update_one(
#             { "_id": rulebook_object_id },
#             {"$set": {"setWizardId": wizard_id}},
#             session = session
#         )
#     return wizard_id

# def get_or_create_setup_wizard(rulebook_id: str) -> dict:
#     """
#     Returns the SETUP_WIZARD document for a rulebook, creating one
#     (atomically, with the Rulebook.setupWizardId backref) if none exists
#     """

#     existing = get_setup_wizard_by_rulebookId(rulebook_id)
#     if existing:
#         return existing

#     try:

#         with client.start_session() as session:
#             session.start_transaction(
#                 lambda s: create_setup_wizard(rulebook_id, session = s)
#             )
#     except DuplicateKeyError:
#             logger.info("Lost setup wizard create race for rule '%s';  re-fetching.", rulebook_id)
#             create_setup_wizard(rulebook_id, session=session)

#     doc = get_setup_wizard_by_rulebookId(rulebook_id)
#     if not doc:
#         raise ValueError(f"Setup wizard for rulebook '{rulebook_id}' not found after creation.")
#     return doc
