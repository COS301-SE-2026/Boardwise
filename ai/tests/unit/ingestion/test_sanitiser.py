from app.ingestion.sanitiser import sanitise_pdf


def test_sanitise_pdf_accepts_clean_pdf(safe_pdf_bytes):
    """Injecting the standard safe bytes fixture from conftest.py"""
    # Act
    is_safe, reason = sanitise_pdf(safe_pdf_bytes)

    # Assert
    assert is_safe is True
    assert reason == ""


def test_sanitise_pdf_rejects_unsafe_js_exploits(unsafe_pdf_js):
    """Injecting the unsafe bytes fixture containing the dangerous /JavaScript tag from conftest.py"""
    # Act
    is_safe, reason = sanitise_pdf(unsafe_pdf_js)

    # Assert
    assert is_safe is False
    assert reason == "Unsafe PDF: Contains /JavaScript"


def test_sanitise_pdf_rejects_unsafe_execution_tags(unsafe_pdf_launch):
    """Injecting the unsafe bytes fixture containing the dangerous /Launch tag from conftest.py"""
    # Act
    is_safe, reason = sanitise_pdf(unsafe_pdf_launch)

    # Assert
    assert is_safe is False
    assert reason == "Unsafe PDF: Contains /Launch"


def test_sanitise_pdf_accepts_safe_pdf_with_exceptions(safe_pdf_with_exceptions):
    """
    Injecting the safe bytes fixture containing the dangerous /JavaScript tag
    located within 100 bytes of a safe exception context from conftest.py
    """
    # Act
    is_safe, reason = sanitise_pdf(safe_pdf_with_exceptions)

    # Assert
    assert is_safe is True
    assert reason == ""
