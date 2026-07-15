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

def test_update_rulebook_status_succeeds_for_valid_rulebook_id(seed_board_game ,seed_user):
    """
    Verifies rulebook status is updated successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]
    contributor_id_str = str(seed_user)

    rulebook_id = mongo_service.create_rulebook(
        title="Dune",
        edition=None,
        contributor_id=contributor_id_str,
        language="en",
        r2_pdf_key=""
    )

    # Act
    result = mongo_service.update_rulebook_status(rulebook_id, "NewStatus")

    # Assert
    saved_doc = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})

    assert result is True
    assert saved_doc is not None
    assert saved_doc["status"] == "NewStatus"
    assert saved_doc["version"] == 0

def test_update_rulebook_r2_pdf_key_succeeds_for_valid_rulebook_id(seed_board_game ,seed_user):
    """
    Verifies rulebook r2 pdf key is updated successfully
    when valid related data exists in the database.
    """
    # Arrange
    db = mongo_service.client[os.environ["DB_NAME"]]
    contributor_id_str = str(seed_user)

    rulebook_id = mongo_service.create_rulebook(
        title="Dune",
        edition=None,
        contributor_id=contributor_id_str,
        language="en",
        r2_pdf_key=""
    )

    # Act
    result = mongo_service.update_rulebook_r2_pdf_key(rulebook_id, "NewR2Key")

    # Assert
    saved_doc = db.RULEBOOK.find_one({"_id": ObjectId(rulebook_id)})

    assert result is True
    assert saved_doc is not None
    assert saved_doc["r2PdfKey"] == "NewR2Key"