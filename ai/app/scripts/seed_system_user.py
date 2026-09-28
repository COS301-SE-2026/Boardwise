import logging
from datetime import datetime, timezone

from bson import ObjectId

from app.config import settings
from app.services.mongo_service import get_db

logger = logging.getLogger(__name__)

SYSTEM_USERNAME = "BOARLEY" # SYSTEM USER
SYSTEM_EMAIL = "scraper@system.boardwise.internal"


def seed_system_user() -> None:
    """
    Ensures the fixed system/scraper user (id = settings.SYSTEM_CONTRIBUTOR_ID)
    exists in the USER collection.
    """
    db = get_db()
    system_id = ObjectId(settings.SYSTEM_CONTRIBUTOR_ID)

    if db["USER"].find_one({"_id": system_id}):
        logger.info("System user already present: %s", system_id)
        return

    now = datetime.now(timezone.utc)
    db["USER"].insert_one(
        {
            "_id": system_id,
            "username": SYSTEM_USERNAME,
            "emailAddress": SYSTEM_EMAIL,
            "password": ObjectId().binary.hex(),  # unusable placeholder, never logs in
            "firstName": "Boardwise",
            "lastName": "Scraper",
            "profilePicture": None,
            "location": None,
            "preferences": {},
            "lastOnlineAt": None,
            "createdAt": now,
            "ownedGames": [],
            "resetToken": None,
            "resetTokenExpiry": None,
        }
    )
    logger.info("Created system user: %s", system_id)