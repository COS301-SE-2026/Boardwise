from unittest.mock import MagicMock, patch

from app.ingestion.extractor import extract_text


def _setup_mock_pdf(mock_fitz_open, page_texts: list[str | None]):
    """
    Helper to reduce code duplication when configuring mocked PDF documents.
    Returns a list of mocked pages for strict invocation assertions.
    """
    mock_pages = []
    for text in page_texts:
        page = MagicMock()
        page.get_text.return_value = text

        mock_pixmap = MagicMock()
        mock_pixmap.tobytes.return_value = b"dummy_image_bytes"
        page.get_pixmap.return_value = mock_pixmap

        mock_pages.append(page)

    mock_document = MagicMock()
    mock_document.__iter__.return_value = mock_pages
    mock_document.__len__.return_value = len(mock_pages)

    mock_fitz_open.return_value.__enter__.return_value = mock_document
    return mock_pages


@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_sufficient_native_text_returns_success(
    mock_fitz_open, safe_pdf_bytes
):
    """Injecting the standard safe bytes fixture from conftest.py and mocking fitz open"""
    # Arrange
    expected_text = "This is a sufficiently long string that will easily bypass the fifty character threshold for native extraction."
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text])

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == expected_text
    assert reason == ""

    mock_fitz_open.assert_called_once_with(stream=safe_pdf_bytes, filetype="pdf")

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_not_called()


@patch("app.ingestion.extractor.pytesseract.image_to_string")
@patch("app.ingestion.extractor.Image.open")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_sparse_text_triggers_ocr_returns_success(
    mock_fitz_open, mock_image_open, mock_ocr, safe_pdf_bytes
):
    """Injecting the standard safe bytes fixture from conftest.py, mocking fitz open, PIL Image, and pytesseract"""
    # Arrange
    expected_text = "Too short."
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text])

    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "This text was successfully extracted via Tesseract OCR."

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == mock_ocr.return_value.strip()
    assert reason == ""

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_called_once_with(dpi=150)
    mock_ocr.assert_called_once_with(mock_image_open.return_value)
    mock_pages[0].get_pixmap.return_value.tobytes.assert_called_once_with("png")


@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_empty_document_returns_false(mock_fitz_open, empty_pdf_bytes):
    """Mocking fitz open"""
    # Arrange
    mock_pages = _setup_mock_pdf(mock_fitz_open, [])

    # Act
    success, text, reason = extract_text(empty_pdf_bytes)

    # Assert
    assert success is False
    assert text == ""
    assert reason == "PDF document is empty."
    assert len(mock_pages) == 0

    mock_fitz_open.assert_called_once_with(stream=empty_pdf_bytes, filetype="pdf")


@patch("app.ingestion.extractor.pytesseract.image_to_string")
@patch("app.ingestion.extractor.Image.open")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_empty_ocr_result_returns_false(
    mock_fitz_open, mock_image_open, mock_ocr, empty_pdf_bytes
):
    """Mocking fitz open and pytesseract image_to_string"""
    # Arrange
    expected_text = ""
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text])

    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = ""

    # Act
    success, text, reason = extract_text(empty_pdf_bytes)

    # Assert
    assert success is False
    assert text == ""
    assert reason == "No readable text or OCR data found in PDF."

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_called_once_with(dpi=150)
    mock_ocr.assert_called_once_with(mock_image_open.return_value)
    mock_pages[0].get_pixmap.return_value.tobytes.assert_called_once_with("png")


@patch("app.ingestion.extractor.logger")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_fitz_exception_returns_false(
    mock_fitz_open, mock_logger, safe_pdf_bytes
):
    """Injecting the standard safe bytes fixture from conftest.py and mocking fitz open"""
    # Arrange
    mock_fitz_open.side_effect = Exception("fitz cannot open file")

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is False
    assert text == ""
    assert reason == "Internal error occurred during text extraction."

    mock_fitz_open.assert_called_once_with(stream=safe_pdf_bytes, filetype="pdf")
    mock_logger.exception.assert_called_once_with("Extraction failed")


@patch("app.ingestion.extractor.pytesseract.image_to_string")
@patch("app.ingestion.extractor.Image.open")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_non_string_native_content_triggers_ocr_returns_success(
    mock_fitz_open, mock_image_open, mock_ocr, safe_pdf_bytes
):
    """Verifies that non-string returns from fitz correctly trigger the OCR fallback"""
    # Arrange
    expected_text = None
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text])

    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "Recovered text via OCR."

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == mock_ocr.return_value.strip()
    assert reason == ""

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_called_once_with(dpi=150)
    mock_ocr.assert_called_once_with(mock_image_open.return_value)
    mock_pages[0].get_pixmap.return_value.tobytes.assert_called_once_with("png")


@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_exactly_fifty_chars_bypasses_ocr_returns_success(
    mock_fitz_open, safe_pdf_bytes
):
    """Boundary test proving that exactly 50 characters skips the OCR processing"""
    # Arrange
    expected_text = "A" * 50
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text])

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == expected_text
    assert reason == ""

    mock_fitz_open.assert_called_once_with(stream=safe_pdf_bytes, filetype="pdf")

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_not_called()


@patch("app.ingestion.extractor.pytesseract.image_to_string")
@patch("app.ingestion.extractor.Image.open")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_forty_nine_chars_triggers_ocr_returns_success(
    mock_fitz_open, mock_image_open, mock_ocr, safe_pdf_bytes
):
    """Boundary test proving that exactly 50 characters skips the OCR processing"""
    # Arrange
    expected_text = "A" * 49
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text])

    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "Recovered text via OCR."

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == mock_ocr.return_value.strip()
    assert reason == ""

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_called_once_with(dpi=150)
    mock_ocr.assert_called_once_with(mock_image_open.return_value)
    mock_pages[0].get_pixmap.return_value.tobytes.assert_called_once_with("png")


@patch("app.ingestion.extractor.pytesseract.image_to_string")
@patch("app.ingestion.extractor.Image.open")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_multiple_pages_returns_joined_text(
    mock_fitz_open, mock_image_open, mock_ocr, safe_pdf_bytes
):
    """Ensures pages are successfully joined with double newlines"""
    # Arrange
    expected_text_1 = "This is page number one, possessing plenty of character length."
    expected_text_2 = ""
    mock_pages = _setup_mock_pdf(mock_fitz_open, [expected_text_1, expected_text_2])

    mock_image_open.return_value = MagicMock()
    mock_ocr.return_value = "Text from page two via OCR."

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == f"{expected_text_1}\n\n{mock_ocr.return_value}"
    assert reason == ""

    mock_pages[0].get_text.assert_called_once()
    mock_pages[0].get_pixmap.assert_not_called()

    mock_pages[1].get_text.assert_called_once()
    mock_pages[1].get_pixmap.assert_called_once_with(dpi=150)

    mock_ocr.assert_called_once_with(mock_image_open.return_value)
    mock_pages[1].get_pixmap.return_value.tobytes.assert_called_once_with("png")
