import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.dependencies import verify_jwt

@pytest.fixture
def client():
    """Provides a TestClient instance for router tests."""
    return TestClient(app)

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
    return (
        "    \n\n"
        "    Setup: Start the game.    \n\n\n\n\n"
        "Next turn tules."
    )

@pytest.fixture
def empty_extracted_text() -> str:
    """String containing no valid semantic data."""
    return " \n\n  \n\n \n"

@pytest.fixture
def empty_pdf_bytes() -> bytes:
    """Empty PDF"""
    return b"%PDF-1.4\n<< >>\n%EOF"
