import os
import pytest

os.environ["DB_NAME"] = "ci_fallback_db"

from bson import ObjectId
from app.services import mongo_service
from datetime import datetime, timezone

@pytest.fixture(autouse=True)
def clean_db():
    """
    Runs before every integration test.
    Wipes all collections to guarantee a clean slate.
    """
    db = mongo_service.client[os.environ["DB_NAME"]]

    db.RULEBOOK.delete_many({})
    db.BOARD_GAME.delete_many({})
    db.USER.delete_many({})
    db.INGESTION_JOB.delete_many({})
    db.RULEBOOK_TEXT.delete_many({})
    db.TOKEN_BLACKLIST.delete_many({})

    yield

@pytest.fixture
def seed_user() -> ObjectId:
    """Inserts a dummy user and returns their exact ObjectId."""
    db = mongo_service.client[os.environ["DB_NAME"]]
    result = db.USER.insert_one({"username": "alice_test"})
    return result.inserted_id

@pytest.fixture
def seed_board_game() -> ObjectId:
    """Inserts a dummy board game and returns its exact ObjectId."""
    db = mongo_service.client[os.environ["DB_NAME"]]
    result = db.BOARD_GAME.insert_one({
        "title": "Dune",
        "imageURL": "https://mocksite.com/dune.png",
        "description": "Galactic conquest"
    })
    return result.inserted_id

@pytest.fixture
def seed_rulebook(seed_board_game, seed_user) -> ObjectId | None:
    """Inserts dummy rulebook and returns its exact ObjectId"""

    db = mongo_service.client[os.environ["DB_NAME"]]
    boardgame = db.BOARD_GAME.find_one({"_id": seed_board_game})
    user = db.USER.find_one({"_id": seed_user})
    now = datetime.now(timezone.utc)

    if boardgame and user:
        result = db.RULEBOOK.insert_one({
            "coverUrl": boardgame.get("imageURL",""),
            "gameId": boardgame["_id"],
            "title": boardgame.get("title", "Unknown"),
            "edition": None,
            "status": "Processing",
            "version": 0,
            "contributorId": seed_user,
            "contributorUsername": user.get("username", "Unknown"),
            "description": boardgame.get("description",""),
            "language": "en",
            "r2PdfKey": "",
            "r2CoverKey": "rulebooks/default_cover.png",
            "lockHeldBy": None,
            "lockExpiresAt": None,
            "undoStack": [],
            "redoStack": [],
            "uploadedAt":now,
            "updatedAt":now,
        })
        return result.inserted_id

    return None
