import pytest
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
from io import BytesIO

from app.services.extractor import extract_text


def make_text_pdf(text: str) -> bytes:
    """
    Generates a real text-based PDF using reportlab.
    """
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.drawString(100, 750, text)
    c.save()
    return buffer.getvalue()


def make_empty_pdf() -> bytes:
    """
    Generates a valid PDF with no text content.
    """
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.save()
    return buffer.getvalue()


# --- successful extraction ---

def test_extract_text_from_text_pdf():
    pdf = make_text_pdf("Catan is a board game.")
    success, text = extract_text(pdf)
    assert success is True
    assert "Catan" in text


def test_extract_text_returns_full_content():
    pdf = make_text_pdf("The spice must flow.")
    success, text = extract_text(pdf)
    assert success is True
    assert "spice" in text.lower()


def test_extract_multipage_pdf():
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.drawString(100, 750, "Page one content.")
    c.showPage()
    c.drawString(100, 750, "Page two content.")
    c.save()
    pdf = buffer.getvalue()

    success, text = extract_text(pdf)
    assert success is True
    assert "Page one" in text
    assert "Page two" in text


# --- failed extraction ---

def test_extract_empty_pdf_fails():
    pdf = make_empty_pdf()
    success, text = extract_text(pdf)
    assert success is False
    assert text == ""


def test_extract_invalid_bytes_fails():
    success, text = extract_text(b"not a pdf")
    assert success is False
    assert text == ""


def test_extract_empty_bytes_fails():
    success, text = extract_text(b"")
    assert success is False
    assert text == ""