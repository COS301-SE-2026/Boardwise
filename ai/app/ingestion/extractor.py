import io
import logging

import fitz
import pytesseract
from PIL import Image

logger = logging.getLogger(__name__)

def extract_text(file_bytes: bytes) -> tuple[bool, str, str]:
    """
    Extracts native text (with OCR fallback) from a PDF
    Returns: (success, extracted_text, failure_reason).
    """
    try:
        with fitz.open(stream=file_bytes, filetype="pdf") as pdf_document:
            if len(pdf_document) == 0:
                return (False, "", "PDF document is empty.")

            extracted_text = []
            for page in pdf_document:
                raw_content = page.get_text()
                if isinstance(raw_content, str):
                    text = raw_content.strip()
                else:
                    text = ""

                # Fallback to OCR if native text is too sparse (e.g., when dealing with scanned images)
                if len(text) < 50:
                    # Capped at 150 DPI to prevent Fargate OOM crashes while maintaining OCR accuracy
                    page_pixmap = page.get_pixmap(dpi=150)
                    pillow_image_object = Image.open(io.BytesIO(page_pixmap.tobytes("png")))
                    text = pytesseract.image_to_string(pillow_image_object).strip()

                if len(text) != 0:
                    extracted_text.append(text)

            final_joined_text = "\n\n".join(extracted_text)

            if len(final_joined_text) == 0:
                return (False, "", "No readable text or OCR data found in PDF.")

            return (True, final_joined_text, "")
    except Exception:
        logger.exception("Extraction failed")
        return (False, "", "Internal error occurred during text extraction.")
