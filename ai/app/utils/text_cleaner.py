import re


def clean_extracted_text(raw_text: str) -> str:
    """
    Normalizes raw PDF OCR text to improve readability
    and format the output for Markdown display.
    """
    if not raw_text:
        return ""

    text = raw_text.replace("\r", "")
    text = re.sub(r"\n\s*\n+", "\n\n", text)

    text = re.sub(r"(?<!\n)\n(?!\n)", " ", text)

    text = re.sub(r"[ \t]", " ", text)

    text = re.sub(
        r"(?i)(?:please\s*)?(note|important|attention):\s*(.*)", r"> **\1:** \2", text
    )

    text = re.sub(r"^([A-Z][A-Z\s]{3,49})$", r"### \1", text, flags=re.MULTILINE)

    return text.strip()
