import os
import pytest

os.environ["DB_NAME"] = "ci_fallback_db"

from bson import ObjectId
from app.services import mongo_service

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
