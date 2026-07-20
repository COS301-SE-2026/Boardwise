import logging

logger = logging.getLogger(__name__)

UNSAFE_PATTERNS = [
    b"/JavaScript",
    b"/AA",
    b"/OpenAction",
    b"/Launch",
    b"/EmbeddedFile",
    b"/XFA"
]

SAFE_CONTEXTS = [
    b"/Type /Catalog",
    b"/AcroForm"
]

def sanitise_pdf(file_bytes: bytes) -> tuple[bool, str]:
    """
    Scans raw PDF bytes for potentially dangerous execution patterns.
    Returns:
        (True, "") if the PDF is safe.
        (False, "failure_reason") if an unsafe pattern is found outside a safe context
    """
    if not file_bytes:
        return (False, "File is empty")
    if not file_bytes.startswith(b"%PDF"):
        return (False, "File bytes are not of a PDF file")

    for pattern in UNSAFE_PATTERNS:
        start_search = 0
        while True:
            pattern_index = file_bytes.find(pattern, start_search)

            if pattern_index == -1:
                break

            # Proximity window
            slice_start = max(0, pattern_index - 100)
            slice_end = min(len(file_bytes), pattern_index + len(pattern) + 100 )
            file_bytes_slice = file_bytes[slice_start:slice_end]

            # Check if any safe context exists in this window
            is_safe = False
            for safe_context in SAFE_CONTEXTS:
                if safe_context in file_bytes_slice:
                    is_safe = True
                    break

            if not is_safe:
                logger.warning("Unsafe PDF: Contains %s", pattern.decode('utf-8'))
                return (False, f"Unsafe PDF: Contains {pattern.decode('utf-8')}")

            start_search = pattern_index + len(pattern)

    return (True, "")
