import os
from unittest.mock import patch

import pytest
from app.dependencies import verify_index_ready
from fastapi.testclient import TestClient

os.environ["DB_NAME"] = "ci_fallback_db"

from datetime import datetime, timezone

from app.main import app
from app.services import mongo_service
from bson import ObjectId


@pytest.fixture(autouse=True)
def clean_db(request):
    """
    Wipes all collections to guarantee a clean slate.
    ONLY runs if the test is explicitly marked with @pytest.mark.db
    """
    if not request.node.get_closest_marker("db"):
        yield
        return

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
    result = db.BOARD_GAME.insert_one(
        {
            "title": "Dune",
            "imageURL": "https://mocksite.com/dune.png",
            "description": "Galactic conquest",
        }
    )
    return result.inserted_id


@pytest.fixture
def seed_rulebook(seed_board_game, seed_user) -> ObjectId | None:
    """Inserts dummy rulebook and returns its exact ObjectId"""

    db = mongo_service.client[os.environ["DB_NAME"]]
    boardgame = db.BOARD_GAME.find_one({"_id": seed_board_game})
    user = db.USER.find_one({"_id": seed_user})
    now = datetime.now(timezone.utc)

    if boardgame and user:
        result = db.RULEBOOK.insert_one(
            {
                "coverUrl": boardgame.get("imageURL", ""),
                "gameId": boardgame["_id"],
                "title": boardgame.get("title", "Unknown"),
                "edition": None,
                "status": "Processing",
                "version": 0,
                "contributorId": seed_user,
                "contributorUsername": user.get("username", "Unknown"),
                "description": boardgame.get("description", ""),
                "language": "en",
                "r2PdfKey": "",
                "r2CoverKey": "rulebooks/default_cover.png",
                "lockHeldBy": None,
                "lockExpiresAt": None,
                "undoStack": [],
                "redoStack": [],
                "uploadedAt": now,
                "updatedAt": now,
            }
        )
        return result.inserted_id

    return None


@pytest.fixture
def mock_pdf_bytes() -> bytes:
    """Returns valid mock pdf bytes"""
    return (
        b"%PDF-1.4\n"
        b"1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n"
        b"2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n"
        b"3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << >> /Contents 4 0 R >>\nendobj\n"
        b"4 0 obj\n<< /Length 48 >>\nstream\n"
        b"BT /F1 12 Tf 50 700 Td (Mock PDF Content) Tj ET\n"
        b"endstream\nendobj\n"
        b"xref\n0 5\n0000000000 65535 f \n"
        b"0000000009 00000 n \n"
        b"0000000058 00000 n \n"
        b"0000000115 00000 n \n"
        b"0000000213 00000 n \n"
        b"trailer\n<< /Size 5 /Root 1 0 R >>\n"
        b"startxref\n310\n"
        b"%%EOF"
    )


@pytest.fixture(autouse=True)
def disable_live_db_connections(request):
    """
    Prevents the FastAPI lifespan from pinging the live MongoDB Atlas
    cluster or Cloudflare R2 buckets during CI/CD.
    If test is marked with 'db', we bypass this mock so the DB can actually connect
    """
    if request.node.get_closest_marker("db"):
        yield None, None, None
        return

    with (
        patch(
            "app.services.mongo_service.ping_database", return_value=True
        ) as mock_mongo_ping,
        patch(
            "app.services.r2_service.ping_r2_storage", return_value=True
        ) as mock_r2_ping,
        patch(
            "app.main.initialise_vector_index", return_value=True
        ) as mock_vector_init,
    ):
        yield mock_mongo_ping, mock_r2_ping, mock_vector_init


# ========== Application Clients & Dependencies ==========


@pytest.fixture
def client(mock_embedder, mock_reranker):
    """
    Provides a TestClient instance for router tests.
    Uses a context manager ('with') to trigger the lifespan boot sequence.
    Patches the model initialisations so app.state.ml_models gets the mocks.
    """
    with (
        patch("app.main.SentenceTransformer") as mock_st,
        patch("app.main.CrossEncoder") as mock_ce,
        patch.object(mongo_service.client, "close"),
    ):
        mock_st.return_value = mock_embedder
        mock_ce.return_value = mock_reranker

        # The 'with' block here is required to trigger @asynccontextmanager
        with TestClient(app) as test_client:
            yield test_client


@pytest.fixture
def mock_verify_index_ready():
    """Mocks the verify_index_ready dependency to bypass MongoDB index checks."""
    app.dependency_overrides[verify_index_ready] = lambda: None

    yield

    app.dependency_overrides.pop(verify_index_ready, None)

@pytest.fixture(scope="session", autouse=True)
def cleanup_db_connect_after_all_tests():
    """Ensures the MongoDB connection is closed after the whole test suite finishes."""
    yield
    from app.services import mongo_service
    mongo_service.client.close()
