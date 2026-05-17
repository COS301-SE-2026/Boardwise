from pypdf import PdfReader
from io import BytesIO


def extract_text(file_bytes: bytes) -> tuple[bool, str]:
    """
    Returns (success, extracted_text).
    success = False means extraction failed - rulebook goes to PendingReview.
    """
    try:
        reader = PdfReader(BytesIO(file_bytes))
        pages = []

        for page in reader.pages:
            text = page.extract_text()
            if text:
                pages.append(text.strip())

        full_text = "\n\n".join(pages)

        if not full_text.strip():
            return False, ""

        return True, full_text

    except Exception:
        return False, ""