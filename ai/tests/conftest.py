from unittest.mock import MagicMock, patch

import pytest
from app.dependencies import verify_jwt
from app.main import app
from fastapi.testclient import TestClient

# ========== Infrastructure Security & ML Mocks ==========


@pytest.fixture(autouse=True)
def disable_live_db_connections():
    """
    Automatically runs before every test.
    Prevents the FastAPI lifespan from pinging the live MongoDB Atlas
    cluster or Cloudflare R2 buckets during CI/CD.
    """
    with (
        patch(
            "app.services.mongo_service.ping_database", return_value=True
        ) as mock_mongo_ping,
        patch(
            "app.services.r2_service.ping_r2_storage", return_value=True
        ) as mock_r2_ping,
    ):
        yield mock_mongo_ping, mock_r2_ping


def mock_ml_models():
    """Provides mock ML models to prevent RAM OOM crashes in Fargate."""
    embedder_mock = MagicMock()
    embedder_mock.encode.return_value = [0.5] * 256

    reranker_mock = MagicMock()
    reranker_mock.predict.return_value = [0.95, 0.80, 0.20]

    return {"embedder": embedder_mock, "reranker": reranker_mock}


# ========== Application Clients & Dependencies ==========


@pytest.fixture
def client():
    """
    Provides a TestClient instance for router tests.
    Uses a context manager ('with') to trigger the lifespan boot sequence.
    Patches the model initialisations so app.state.ml_models gets the mocks.
    """
    with (
        patch("app.main.SentenceTransformer") as mock_st,
        patch("app.main.CrossEncoder") as mock_ce,
    ):
        mock_st.return_value = mock_ml_models()["embedder"]
        mock_ce.return_value = mock_ml_models()["reranker"]

        # The 'with' block here is required to trigger @asynccontextmanager
        with TestClient(app) as test_client:
            yield test_client


@pytest.fixture
def mock_auth():
    """Overrides the JWT dependency to simulate an authenticated user."""

    def override_verify_jwt():
        return {"sub": "609c12345678901234567890", "username": "TestUser"}

    app.dependency_overrides[verify_jwt] = override_verify_jwt
    yield

    app.dependency_overrides.clear()


# ========== Sanitiser fixtures ==========


@pytest.fixture
def safe_pdf_bytes() -> bytes:
    """A standard, harmless PDF byte structure."""
    return b"%PDF-1.4\n1 0 obj\n<< /Type /Pages /Count 1 >>\nendobj\n%EOF"


@pytest.fixture
def unsafe_pdf_js() -> bytes:
    """PDF containing the dangerous /JavaScript tag."""
    return b"%PDF-1.4\n<< /JavaScript (alert('Exploit')) >>\n%EOF"


@pytest.fixture
def unsafe_pdf_launch() -> bytes:
    """PDF containing the dangerous /Launch tag."""
    return b"%PDF-1.4\n<< /Launch /Action >>\n%EOF"


@pytest.fixture
def safe_pdf_with_exceptions() -> bytes:
    """
    PDF containing a 'dangerous' keyword but safely located within
    100 bytes of a safe exception context (/Type /Catalog)
    """
    return b"%PDF-1.4\n<< /Type /Catalog ... /JavaScript >>\n%EOF"


# ========== Extractor and Chunker fixtures ==========


@pytest.fixture
def standard_extracted_text() -> str:
    """Clean, predictable text with standard double-newline breaks."""
    return (
        "Setup: Each player takes a player board and 5 starting tokens.\n\n"
        "Phase 1: Draw two cards from the central deck.\n\n"
        "Phase 2: Play a card from your hand or pass to the next player."
    )


@pytest.fixture
def messy_extracted_text() -> str:
    """Text designed to test whitespace stripping and empty chunk dropping."""
    return "    \n\n    Setup: Start the game.    \n\n\n\n\nNext turn tules."


@pytest.fixture
def empty_extracted_text() -> str:
    """String containing no valid semantic data."""
    return " \n\n  \n\n \n"


@pytest.fixture
def empty_pdf_bytes() -> bytes:
    """Empty PDF"""
    return b"%PDF-1.4\n<< >>\n%EOF"
