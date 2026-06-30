import io
import logging
import fitz
import pytesseract
from PIL import Image

logger = logging.getLogger(__name__)

def extract_text_and_cover(file_bytes: bytes) -> tuple[bool, str, bytes | None]:
    """
    Extracts natve text (with OCR fallback) and a PNG cover image from a PDF
    Returns: (success, extracted_text, first_page_image_bytes).
    """
    try:
        # 1. Initialisation
        with fitz.open(stream=file_bytes, filetype="pdf") as pdf_document:
            if len(pdf_document) == 0:
                return (False, "", None)

            # 2. Extract Cover Image
            first_page = pdf_document[0]
            first_page_pixmap = first_page.get_pixmap(dpi=150)
            cover_bytes = first_page_pixmap.tobytes("png")

            # 3. Text Extraction and Processing
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

            # 4. Validation
            final_joined_text = "\n\n".join(extracted_text)

            if len(final_joined_text) == 0:
                return (False, "", cover_bytes)

            return (True, final_joined_text, cover_bytes)
    except Exception as e:
        logger.error(f"Extraction failed: {str(e)}", exc_info=True)
        return (False, "", None)