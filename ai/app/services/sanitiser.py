from pypdf import PdfReader
from io import BytesIO


UNSAFE_PATTERNS = [
    "/JavaScript",
    # "/JS",
    "/AA",
    "/OpenAction",
    "/Launch",
    "/EmbeddedFile",
    "/XFA"
]

SAFE_EXCEPTIONS = [
    "/Type /Catalog",
    "/AcroForm"
]


def sanitise_pdf(file_bytes: bytes) -> tuple[bool, str]:
    try:
        reader = PdfReader(BytesIO(file_bytes))
    except Exception:
        return False, "File could not be parsed as a valid PDF"

    raw_content = file_bytes.decode("latin-1", errors="ignore")

    for pattern in UNSAFE_PATTERNS:
        if pattern in raw_content:
            # check if it appears in a safe context
            pattern_index = raw_content.find(pattern)
            surrounding = raw_content[max(0, pattern_index - 50):pattern_index + 50]

            is_safe_context = any(
                exception in surrounding for exception in SAFE_EXCEPTIONS
            )

            if not is_safe_context:
                return False, f"Unsafe PDF content detected: {pattern}"

    return True, ""