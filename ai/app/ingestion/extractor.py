import io
import json
import logging
import re
import uuid
from typing import Any, cast

import pymupdf
import pymupdf4llm
import requests

from app.config import settings
from app.services.r2_service import upload_to_r2

logger = logging.getLogger(__name__)


def extract_text(file_bytes: bytes, rulebook_id: str) -> tuple[bool, str, str, dict]:
    """
    Extracts native Markdown (with automatic OCR fallback) from a PDF
    Returns: (success, extracted_markdown, failure_reason, blocks_cache).
    """
    try:
        with pymupdf.open(stream=file_bytes, filetype="pdf") as pdf_document:
            if len(pdf_document) == 0:
                return (False, "", "PDF document is empty.", {})

            raw_md = pymupdf4llm.to_markdown(pdf_document, page_chunks=True)

            if isinstance(raw_md, str):
                return (False, "", "Unexpected string output from extractor.", {})

            md_dicts = cast(list[dict[str, Any]], raw_md)

            blocks_cache = {
                "rulebookId": rulebook_id,
                "extractorVersion": "tier2-v1",
                "blocks": [],
            }
            
            used_tier3 = False

            if _fails_quality_check(md_dicts):
                tier3_result = _escalate_to_tier_3(file_bytes)
                md_dicts = cast(list[dict[str, Any]], tier3_result)
                blocks_cache["extractorVersion"] = "tier3-v1"
                used_tier3 = True

            for page_num, page_dict in enumerate(md_dicts):
                if not used_tier3:
                    raw_page = pdf_document[page_num]
                    raw_dict = raw_page.get_text("dict")

                    page_dict["blocks"] = (
                        raw_dict.get("blocks", []) if isinstance(raw_dict, dict) else []
                    )

                blocks_cache["blocks"].extend(_map_to_blocks(page_dict, rulebook_id))

            flat_text_pieces = []
            for block in blocks_cache["blocks"]:
                content = block.get("content")
                if content:
                    flat_text_pieces.append(str(content))
            flat_text = "\n".join(flat_text_pieces)

            cache_key = f"rulebooks/{rulebook_id}/blocks_v1.json"
            upload_to_r2(
                json.dumps(blocks_cache).encode("utf-8"), cache_key, "application/json"
            )

            return (True, flat_text.strip(), "", blocks_cache)
    except Exception:
        logger.exception("Extraction failed")
        return (False, "", "Internal error occurred during text extraction.", {})


def _map_to_blocks(page_dict: dict[str, Any], rulebook_id: str) -> list[dict]:
    """Transforms a page dictionary into the block-level schema"""
    blocks = []

    metadata = page_dict.get("metadata", {})
    page_num = metadata.get("page", 1) if isinstance(metadata, dict) else 1

    raw_blocks = page_dict.get("blocks", [])
    if not isinstance(raw_blocks, list):
        raw_blocks = []

    for order, raw_block in enumerate(raw_blocks):
        if not isinstance(raw_block, dict):
            continue

        block_id = f"blk_{uuid.uuid4().hex[:8]}"
        block_type = "paragraph"
        content = ""
        confidence = 1.0
        image_url = None
        heading_level = None

        if raw_block.get("type") == 1:  # Image block in pymupdf
            block_type = "image"
            image_bytes = raw_block.get("image")
            image_ext = raw_block.get("ext", "png")

            image_key = f"rulebooks/{rulebook_id}/pending_image_{block_id}.{image_ext}"
            image_url = f"{settings.R2_RULEBOOKS_URL}{image_key}"
            confidence = 0.95

            if image_bytes:
                upload_to_r2(image_bytes, image_key, f"image/{image_ext}")

        elif raw_block.get("type") == 0:  # Text block
            lines = raw_block.get("lines", [])
            if isinstance(lines, list):
                content = "\n".join(
                    "".join(
                        span.get("text", "")
                        for span in line.get("spans", [])
                        if isinstance(span, dict)
                    )
                    for line in lines
                    if isinstance(line, dict)
                )

            if content.startswith("#"):
                block_type = "heading"
                match = re.match(r"^#+", content)
                heading_level = len(match.group(0)) if match else 1
            elif "|" in content and content.count("|") > 3:
                block_type = "table"
                confidence = 0.7

        if block_type == "table":
            pass
        elif block_type != "image" and (
            not content.strip() or _is_ocr_gibberish(content)
        ):
            continue

        blocks.append(
            {
                "blockId": block_id,
                "order": order,
                "type": block_type,
                "content": content.strip(),
                "confidence": confidence,
                "sourcePage": page_num,
                "imageUrl": image_url,
                "headingLevel": heading_level,
            }
        )

    return blocks


def _fails_quality_check(md_dicts: list[dict]) -> bool:
    """
    Heuristic to check if extraction is poor.
    md_dicts is initial output from pymupdf4llm
    """
    if not md_dicts:
        return True

    total_chars = 0
    mixed_up_chars = 0
    pages = len(md_dicts)

    for page in md_dicts:
        text = page.get("text", "")
        total_chars += len(text)

        mixed_up_chars += len(re.findall(r"[^\x00-\x7F\xA0-\xFF]", text))

    avg_chars_per_page = (total_chars / pages) if pages > 0 else 0

    if pages > 2 and avg_chars_per_page < 100:
        return True

    return (
        total_chars > 0 and (mixed_up_chars / total_chars) > 0.15
    )  # True if high proportion of mixed up characters are present


def _escalate_to_tier_3(file_bytes: bytes) -> list[dict]:
    """
    Extraction path reserved for rulebooks failing the quality check.
    """

    api_url = settings.UNSTRUCTURED_API_URL
    if not api_url:
        logger.error("Unstructured api url is absent")
        return []
    headers = {}
    
    if hasattr(settings, "INTERNAL_WEBHOOK_SECRET") and settings.INTERNAL_WEBHOOK_SECRET:
        headers["unstructured-api-key"] = settings.INTERNAL_WEBHOOK_SECRET

    try:
        response = requests.post(
            api_url,
            headers=headers,
            files={
                "files": ("rulebook.pdf", io.BytesIO(file_bytes), "application/pdf")
            },
            data={"strategy": "hi_res", "pdf_infer_table_structure": "true"},
            timeout=120,
        )
        response.raise_for_status()
        elements = response.json()

        pages = {}
        for el in elements:
            page_num = el.get("metadata", {}).get("page_number", 1)

            if page_num not in pages:
                pages[page_num] = {"text": "", "blocks": []}

            mapped_type = 1 if el.get("type") == "Image" else 0

            pages[page_num]["blocks"].append(
                {
                    "type": mapped_type,
                    "lines": [{"spans": [{"text": el.get("text", "")}]}],
                }
            )

        return [pages[p] for p in sorted(pages.keys())]
    except requests.exceptions.Timeout:
        logger.error(
            "Tier 3 extraction timed out. The local Docker container was too slow."
        )
        return []
    except requests.exceptions.ConnectionError:
        logger.error(
            "Tier 3 connection dropped. The Docker container likely hit its memory limit."
        )
        return []
    except requests.exceptions.HTTPError as e:
        logger.error(
            f"Tier 3 API returned an HTTP error(likely an OOM crash inside the container): {e}"
        )
        return []
    except Exception:
        logger.exception("Unexpected error communicating with local Unstructured API.")
        return []


def _is_ocr_gibberish(text: str) -> bool:
    """Determines if OCR extracted text is just random characters"""
    text = text.strip()
    if not text:
        return True

    # Fail on OCR noise characters that are easiest to detect
    if any(char in text for char in "@~^°¢¤¥§\\"):
        return True

    # Check for mid-word non-alphanumerics (e.g., "l@DQe", "a#b")
    if re.search(r"[a-zA-Z][^a-zA-Z0-9\s.,!?:;\'\"()\-/][a-zA-Z]", text):
        return True

    # Check for OCR casing errors (lowercase immediately followed by uppercase, e.g. "aN", "dOO")
    if re.search(r"\b[a-z]+[A-Z]+", text):
        return True

    # Check for micro-line vertical token stacking (3+ lines averaging < 6 characters)
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    if len(lines) >= 3 and (sum(len(line) for line in lines) / len(lines)) < 6:
        return True

    # Checking the special character to normal alphanumeric character ratio (Higher than 0.15 = gibberish)
    special_chars = len(re.findall(r"[^a-zA-Z0-9\s.,!?:;\'\"()\-/]", text))
    if len(text) > 0 and (special_chars / len(text)) > 0.15:
        return True

    # Checking for vowel-less tokens longer than 2 characters (e.g, "SS")
    words = [re.sub(r"[^a-zA-Z]", "", w) for w in text.split()]
    words = [w for w in words if len(w) >= 3]
    if words:
        vowelless = [w for w in words if not re.search(r"[aeiouyAEIOUY]", w)]

        # If more than 1/4 of the significant words lack vowels, it's probably noise
        if (len(vowelless) / len(words)) > 0.25:
            return True

    return False
