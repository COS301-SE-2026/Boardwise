import os
import pytest
from app.services import mongo_service
from bson import ObjectId

def test_create_rulebook_success(seed_board_game, seed_user):
    """
    Verifies a rulebook is created successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]
    contributor_id_str = str(seed_user)

    # Act
    rulebook_id = mongo_service.create_rulebook(
        title="Dune",
        edition=None,
        contributor_id=contributor_id_str,
        language="en",
        r2_pdf_key=""
    )

    # Assert
    saved_doc = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})

    assert saved_doc is not None
    assert saved_doc["gameId"] == seed_board_game
    assert saved_doc["contributorUsername"] == "alice_test"
    assert saved_doc["title"] == "Dune"

    assert saved_doc["status"] == "Processing"
    assert saved_doc["version"] == 0
    assert saved_doc["r2PdfKey"] == ""

    assert saved_doc["lockHeldBy"] is None
    assert saved_doc["lockExpiresAt"] is None
    assert saved_doc["undoStack"] == []
    assert saved_doc["redoStack"] == []

    assert saved_doc["uploadedAt"] == saved_doc["updatedAt"]

def test_create_rulebook_failure_when_board_game_not_found(seed_board_game ,seed_user):
    """
    Verifies that rulebook creation fails when
    associated board game does not exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]
    contributor_id_str = str(seed_user)

    with pytest.raises(ValueError, match=r"Boardgame 'DoesNotExist' not found.") as exc_info:
        # Act
        mongo_service.create_rulebook(
            title="DoesNotExist",
            edition=None,
            contributor_id=contributor_id_str,
            language="en",
            r2_pdf_key=""
        )

    # Assert
    assert db.RULEBOOK.find_one({"title": "DoesNotExist"}) is None
    assert exc_info.type is ValueError
    assert "Boardgame 'DoesNotExist' not found." in exc_info.value.args

def test_create_rulebook_failure_when_contributor_id_not_found(seed_board_game ,seed_user):
    """
    Verifies that rulebook creation fails when
    associated contributor (user) does not exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]
    contributor_id_str = str(ObjectId())

    with pytest.raises(ValueError, match=f"User '{contributor_id_str}' not found.") as exc_info:
        # Act
        mongo_service.create_rulebook(
            title="Dune",
            edition=None,
            contributor_id=contributor_id_str,
            language="en",
            r2_pdf_key=""
        )

    # Assert
    assert db.RULEBOOK.find_one({"title": "Dune"}) is None
    assert exc_info.type is ValueError
    assert f"User '{contributor_id_str}' not found." in exc_info.value.args

def test_update_rulebook_status_succeeds_for_valid_rulebook_id(seed_rulebook):
    """
    Verifies rulebook status is updated successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(seed_rulebook)

    # Act
    mongo_service.update_rulebook_status(rulebook_id, "NewStatus")

    # Assert
    saved_doc = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})

    assert saved_doc is not None
    assert saved_doc["status"] == "NewStatus"
    assert saved_doc["version"] == 0

def test_update_rulebook_r2_pdf_key_succeeds_for_valid_rulebook_id(seed_rulebook):
    """
    Verifies rulebook r2 pdf key is updated successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(seed_rulebook)

    # Act
    mongo_service.update_rulebook_r2_pdf_key(rulebook_id, "NewR2Key")

    # Assert
    saved_doc = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})

    assert saved_doc is not None
    assert saved_doc["r2PdfKey"] == "NewR2Key"

def test_create_ingestion_job_success(seed_rulebook):
    """
    Verifies an ingestion job is created successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(seed_rulebook)

    # Act
    job_id = mongo_service.create_ingestion_job(rulebook_id)

    # Assert
    saved_doc = db.INGESTION_JOB.find_one({"_id": ObjectId(job_id)})

    assert saved_doc is not None
    assert saved_doc["rulebookId"] == ObjectId(rulebook_id)
    assert saved_doc["stage"] == "Sanitise"
    assert saved_doc["jobStatus"] == "Processing"
    assert saved_doc["failureReason"] is None
    assert saved_doc["completedAt"] is None

def test_create_ingestion_job_failure_when_rulebook_not_found(seed_rulebook):
    """
    Verifies ingestion job creation fails when
    rulebook does not exist in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(ObjectId())
    
    with pytest.raises(ValueError, match=f"Rulebook '{rulebook_id}' not found.") as exc_info:
        # Act
        mongo_service.create_ingestion_job(rulebook_id)

    # Assert
    assert db.INGESTION_JOB.find_one({}) is None
    assert exc_info.type is ValueError
    assert f"Rulebook '{rulebook_id}' not found." in exc_info.value.args

def test_update_ingestion_job_success_for_valid_job_id(seed_rulebook):
    """
    Verifies ingestion job is updated successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(seed_rulebook)

    job_id = mongo_service.create_ingestion_job(rulebook_id)

    # Act
    mongo_service.update_ingestion_job(job_id,"Storage", "Completed")

    # Assert
    saved_doc = db.INGESTION_JOB.find_one({"_id": ObjectId(job_id)})

    assert saved_doc is not None
    assert saved_doc["rulebookId"] == ObjectId(rulebook_id)
    assert saved_doc["stage"] == "Storage"
    assert saved_doc["jobStatus"] == "Completed"
    assert saved_doc["failureReason"] == ""
    assert saved_doc["completedAt"] is not None

def test_get_ingestion_job_success(seed_rulebook):
    """Verifies that get ingestion job retrieves the specified ingestion job"""
    # Arrange
    rulebook_id = str(seed_rulebook)
    job_id = mongo_service.create_ingestion_job(rulebook_id)

    # Act
    saved_doc = mongo_service.get_ingestion_job(job_id)

    # Assert
    assert saved_doc is not None
    assert saved_doc["rulebookId"] == rulebook_id
    assert saved_doc["stage"] == "Sanitise"
    assert saved_doc["jobStatus"] == "Processing"
    assert saved_doc["failureReason"] is None
    assert saved_doc["completedAt"] is None

def test_create_rulebook_text_success(seed_rulebook):
    """
    Verifies a rulebook text is created successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(seed_rulebook)

    chunks_list = [
        {
            "chunkId": ObjectId(),
            "index": 0,
            "content": "Content for the first chunk."
        },
        {
            "chunkId": ObjectId(),
            "index": 1,
            "content": "Content for the second chunk."
        },
        {
            "chunkId": ObjectId(),
            "index": 2,
            "content": "Content for the third chunk."
        }
    ]

    # Act
    job_id = mongo_service.create_rulebook_text(rulebook_id, chunks_list)

    # Assert
    saved_doc = db.RULEBOOK_TEXT.find_one({"_id": ObjectId(job_id)})

    assert saved_doc is not None
    assert saved_doc["rulebookId"] == ObjectId(rulebook_id)
    assert saved_doc["version"] == 0
    assert saved_doc["chunks"][0]["content"] == "Content for the first chunk."

def test_create_rulebook_text_failure_when_rulebook_not_found(seed_rulebook):
    """
    Verifies rulebook text creation fails when
    the rulebook does not exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]

    rulebook_id = str(ObjectId())

    with pytest.raises(ValueError, match=f"Rulebook '{rulebook_id}' not found.") as exc_info:
        # Act
        mongo_service.create_rulebook_text(rulebook_id, [])

    # Assert
    assert db.RULEBOOK_TEXT.find_one({}) is None
    assert exc_info.type is ValueError
    assert f"Rulebook '{rulebook_id}' not found." in exc_info.value.args

def test_is_token_valid_success():
    """Verifies that the check passes if the token is not blacklisted"""
    # Arrange
    jti = str(ObjectId())

    # Act
    valid = mongo_service.is_token_valid(jti)

    # Assert
    assert valid is True

def test_is_token_valid_failure_for_revoked_jti():
    """Verifies that the check fails if the token is blacklisted"""
    # Arrange
    jti = "revoked_fr_123"
    db = mongo_service.client[os.environ["DB_NAME"]]
    db.TOKEN_BLACKLIST.insert_one({"_id": jti})

    # Act
    valid = mongo_service.is_token_valid(jti)

    # Assert
    assert valid is False