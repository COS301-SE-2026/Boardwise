from unittest.mock import MagicMock, patch

from app.ingestion.extractor import extract_text


def _setup_mock_pdf(mock_fitz_open, document_length: int = 1):
    """
    Helper to reduce code duplication when configuring mocked PDF documents.
    """
    mock_document = MagicMock()
    mock_document.__len__.return_value = document_length
    
    mock_fitz_open.return_value.__enter__.return_value = mock_document
    return mock_document

@patch("app.ingestion.extractor.pymupdf4llm.to_markdown")
@patch("app.ingestion.extractor.fitz.open")
def test_extract_text_successful_parsing_returns_markdown(
    mock_fitz_open, mock_to_markdown, safe_pdf_bytes
):
    # Arrange
    expected_md = "# Header\n\nThis is extracted markdown text."
    mock_document = _setup_mock_pdf(mock_fitz_open,5)
    mock_to_markdown.return_value = expected_md

    # Act
    success, text, reason = extract_text(safe_pdf_bytes)

    # Assert
    assert success is True
    assert text == expected_md
    assert reason == ""

    mock_fitz_open.assert_called_once_with(stream=safe_pdf_bytes, filetype="pdf")

    mock_to_markdown.assert_called_once_with(mock_document, page_chunks=False)