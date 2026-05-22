# tests/test_sanitiser.py
import pytest
from reportlab.pdfgen import canvas
from reportlab.lib.pagesizes import letter
from io import BytesIO
from app.services.sanitiser import sanitise_pdf


def make_valid_pdf() -> bytes:
    buffer = BytesIO()
    c = canvas.Canvas(buffer, pagesize=letter)
    c.drawString(100, 750, "Safe content.")
    c.save()
    return buffer.getvalue()


def inject_keyword(pdf_bytes: bytes, keyword: str) -> bytes:
    # append the unsafe keyword as a comment at the end of the PDF
    return pdf_bytes + f"\n%% {keyword}\n".encode("latin-1")


# --- safe PDFs ---

def test_safe_pdf_passes():
    pdf = make_valid_pdf()
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is True
    assert reason == ""


def test_safe_pdf_with_catalog_passes():
    pdf = make_valid_pdf()
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is True
    assert reason == ""


# --- unsafe PDFs ---

def test_pdf_with_open_action_fails():
    pdf = inject_keyword(make_valid_pdf(), "/OpenAction")
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is False
    assert "OpenAction" in reason


def test_pdf_with_launch_fails():
    pdf = inject_keyword(make_valid_pdf(), "/Launch")
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is False
    assert "Launch" in reason


def test_pdf_with_embedded_file_fails():
    pdf = inject_keyword(make_valid_pdf(), "/EmbeddedFile")
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is False
    assert "EmbeddedFile" in reason


def test_pdf_with_xfa_fails():
    pdf = inject_keyword(make_valid_pdf(), "/XFA")
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is False
    assert "XFA" in reason


def test_pdf_with_aa_fails():
    pdf = inject_keyword(make_valid_pdf(), "/AA")
    is_safe, reason = sanitise_pdf(pdf)
    assert is_safe is False
    assert "AA" in reason


# --- invalid files ---

def test_invalid_file_fails():
    is_safe, reason = sanitise_pdf(b"this is not a pdf")
    assert is_safe is False
    assert reason != ""


def test_empty_bytes_fails():
    is_safe, reason = sanitise_pdf(b"")
    assert is_safe is False
    assert reason != ""