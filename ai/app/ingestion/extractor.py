import logging

import fitz
import pymupdf4llm

logger = logging.getLogger(__name__)


def extract_text(file_bytes: bytes) -> tuple[bool, str, str]:
    """
    Extracts native Markdown (with automatic OCR fallback) from a PDF
    Returns: (success, extracted_markdown, failure_reason).
    """
    try:
        with fitz.open(stream=file_bytes, filetype="pdf") as pdf_document:
            if len(pdf_document) == 0:
                return (False, "", "PDF document is empty.")

            md_text = pymupdf4llm.to_markdown(
                pdf_document,
                # Set to True to get a list of dictionaries with page-level metadata for vector search chunking
                page_chunks=False,
            )

            if not isinstance(md_text, str):
                return (False, "", "Unexpected format returned from PDF parser.")

            if not md_text or len(md_text.strip()) == 0:
                return (False, "", "No readable text or OCR data found in PDF.")

            clean_md = md_text.strip()

            return (True, clean_md, "")
    except Exception:
        logger.exception("Extraction failed")
        return (False, "", "Internal error occurred during text extraction.")
