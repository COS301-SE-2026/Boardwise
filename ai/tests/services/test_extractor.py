from unittest.mock import patch, MagicMock
from app.services.extractor import extract_text

@patch("app.services.extractor.fitz.open")
def test_extract_text_accepts_native_pdf_text(mock_fitz_open, safe_pdf_bytes):
    """Injecting the standard safe bytes fixture from conftest.py and mocking fitz open"""
    mock_page = MagicMock()
    mock_page.get_text.return_value = "This is a sufficiently long string that will easily bypass the fifty character threshold for native extraction."

    mock_document = MagicMock()
    mock_document.__iter__.return_value = [mock_page]
    mock_document.__len__.return_value = 1

    mock_fitz_open.return_value.__enter__.return_value = mock_document

    success, text = extract_text(safe_pdf_bytes)

    assert success is True
    assert "bypass the fifty character threshold" in text

    mock_fitz_open.assert_called_once_with(stream=safe_pdf_bytes, filetype="pdf")

    mock_page.get_text.assert_called_once()
    mock_page.get_pixmap.assert_not_called()

@patch("app.services.extractor.pytesseract.image_to_string")
@patch("app.services.extractor.Image.open")
@patch("app.services.extractor.fitz.open")
def test_extract_text_accepts_text_embedded_in_image(mock_fitz_open, mock_image_open,mock_ocr, safe_pdf_bytes):
    """Injecting the standard safe bytes fixture from conftest.py, mocking fitz open, PIL Image, and pytesseract"""
    mock_page = MagicMock()
    mock_page.get_text.return_value = "Too short."

    mock_pixmap = MagicMock()
    mock_pixmap.tobytes.return_value = b"dummy_image_bytes"
    mock_page.get_pixmap.return_value = mock_pixmap

    mock_document = MagicMock()
    mock_document.__iter__.return_value = [mock_page]
    mock_document.__len__.return_value = 1

    mock_fitz_open.return_value.__enter__.return_value = mock_document

    mock_image_open.return_value = MagicMock()

    mock_ocr.return_value = "This text was successfully extracted via Tesseract OCR."

    success, text = extract_text(safe_pdf_bytes)

    assert success is True
    assert "extracted via Tesseract" in text

    mock_page.get_text.assert_called_once()
    mock_page.get_pixmap.assert_called_once_with(dpi=300)
    mock_ocr.assert_called_once()

@patch("app.services.extractor.fitz.open")
def test_extract_text_rejects_empty_native_document(mock_fitz_open, empty_pdf_bytes):
    """Mocking fitz open"""
    mock_page = MagicMock()
    mock_page.get_text.return_value = ""

    mock_document = MagicMock()
    mock_document.__iter__.return_value = [mock_page]
    mock_document.__len__.return_value = 0

    mock_fitz_open.return_value.__enter__.return_value = mock_document

    success, text = extract_text(empty_pdf_bytes)

    assert success is False
    assert text == ""

    mock_fitz_open.assert_called_once_with(stream=empty_pdf_bytes, filetype="pdf")

    mock_page.get_pixmap.assert_not_called()

@patch("app.services.extractor.pytesseract.image_to_string")
@patch("app.services.extractor.Image.open")
@patch("app.services.extractor.fitz.open")
def test_extract_text_rejects_empty_image_document(mock_fitz_open, mock_image_open, mock_ocr, empty_pdf_bytes):
    """Mocking fitz open and pytesseract image_to_string"""
    mock_page = MagicMock()
    mock_page.get_text.return_value = ""

    mock_pixmap = MagicMock()
    mock_pixmap.tobytes.return_value = b"dummy_image_bytes"
    mock_page.get_pixmap.return_value = mock_pixmap

    mock_document = MagicMock()
    mock_document.__iter__.return_value = [mock_page]
    mock_document.__len__.return_value = 1

    mock_fitz_open.return_value.__enter__.return_value = mock_document

    mock_image_open.return_value = MagicMock()

    mock_ocr.return_value = ""

    success, text = extract_text(empty_pdf_bytes)

    assert success is False
    assert text == ""

    mock_page.get_text.assert_called_once()
    mock_page.get_pixmap.assert_called_once_with(dpi=300)
    mock_ocr.assert_called_once()

@patch("app.services.extractor.fitz.open")
def test_extract_text_rejects_corrupt_pdf(mock_fitz_open, safe_pdf_bytes):
    """Injecting the standard safe bytes fixture from conftest.py and mocking fitz open"""
    mock_fitz_open.side_effect = Exception("fitz cannot open file")

    success, text = extract_text(safe_pdf_bytes)

    assert success is False
    assert text == ""

    mock_fitz_open.assert_called_once_with(stream=safe_pdf_bytes, filetype="pdf")