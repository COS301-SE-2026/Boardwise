import io
import logging
import fitz
import pytesseract
from PIL import Image

logger = logging.getLogger(__name__)

def extract_text(file_bytes: bytes) -> tuple[bool, str]:
    """
    Extracts native text (with OCR fallback) from a PDF
    Returns: (success, extracted_text).
    """
    try:
        # 1. Initialisation
        with fitz.open(stream=file_bytes, filetype="pdf") as pdf_document:
            if len(pdf_document) == 0:
                return (False, "")

            #2. Text Extraction and Processing
            extracted_text = []
            for page in pdf_document:
                text = page.get_text().strip()

                # OCR Fallback
                if len(text) < 50:
                    page_pixmap = page.get_pixmap(dpi=300)
                    pillow_image_object = Image.open(io.BytesIO(page_pixmap.tobytes("png")))
                    text = pytesseract.image_to_string(pillow_image_object).strip()

                if len(text) != 0:
                    extracted_text.append(text)

            # 3. Validation
            final_joined_text = "\n\n".join(extracted_text)

            if len(final_joined_text) == 0:
                return (False, "")

            return (True, final_joined_text)
    except Exception as e:
        logger.exception("Extraction failed")
        return (False, "")