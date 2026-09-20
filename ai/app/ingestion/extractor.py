import base64
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


def _extract_text_from_raw_block(raw_block: dict) -> str:
    """Helper to extract plain text from a PyMuPDF text block for caption heuristics."""
    if raw_block.get("type") != 0:
        return ""

    lines = raw_block.get("lines", [])
    if not isinstance(lines, list):
        return ""

    content_lines = []
    for line in lines:
        if not isinstance(line, dict):
            continue
        span_texts = []
        for span in line.get("spans", []):
            if isinstance(span, dict):
                span_texts.append(span.get("text", ""))
        content_lines.append("".join(span_texts))

    # Join with spaces instead of newlines for a beter caption preview
    return " ".join(content_lines).strip()


def _map_to_blocks(page_dict: dict[str, Any], rulebook_id: str) -> list[dict]:
    """Transforms a page dictionary into the block-level schema"""
    blocks = []

    metadata = page_dict.get("metadata", {})
    page_num = metadata.get("page", 1) if isinstance(metadata, dict) else 1

    raw_blocks = page_dict.get("blocks", [])
    if not isinstance(raw_blocks, list):
        raw_blocks = []

    # Calculate the median font size for the page such that a baseline size can be established
    font_sizes = []
    for raw_block in raw_blocks:
        if isinstance(raw_block, dict) and raw_block.get("type") == 0:
            for line in raw_block.get("lines", []):
                if isinstance(line, dict):
                    for span in line.get("spans", []):
                        if isinstance(span, dict) and "size" in span:
                            font_sizes.append(span["size"])

    if font_sizes:
        font_sizes.sort()
        median_size = font_sizes[len(font_sizes) // 2]
    else:
        median_size = 11.0  # Fallback

    def _block_sort_key(block: Any) -> tuple[float, float]:
        if isinstance(block, dict):
            bbox = block.get("bbox")
            if isinstance(bbox, (list, tuple)) and len(bbox) >= 2:
                return ((float(bbox[1])), float(bbox[0]))
        # Invalid blocks pushed to the end
        return ((float("inf")), float("inf"))

    raw_blocks.sort(key=_block_sort_key)

    for order, raw_block in enumerate(raw_blocks):
        if not isinstance(raw_block, dict):
            continue

        block_id = f"blk_{uuid.uuid4().hex[:8]}"
        block_type = "paragraph"
        content = ""
        confidence = 1.0
        image_url = None
        heading_level = None
        block_max_size = 0.0

        if raw_block.get("type") == 1:  # Image block in pymupdf
            block_type = "image"
            image_bytes = raw_block.get("image")
            image_ext = raw_block.get("ext", "png")

            image_key = f"rulebooks/{rulebook_id}/images/{page_num}_{order}.{image_ext}"
            image_url = f"{settings.R2_RULEBOOKS_URL}{image_key}"
            confidence = 0.95

            if image_bytes:
                upload_to_r2(image_bytes, image_key, f"image/{image_ext}")

            caption_text = ""

            # Look ahead to next block
            if (order + 1) < len(raw_blocks):
                next_text = _extract_text_from_raw_block(raw_blocks[order + 1])

                # Checking if it looks like a caption (short text or is explicitly labeled)
                if next_text and (
                    len(next_text) < 200
                    or next_text.lower().startswith(("fig", "image", "table"))
                ):
                    caption_text = next_text

            # Look behind (above) if nothing was found below
            if not caption_text and (order - 1 >= 0):
                prev_text = _extract_text_from_raw_block(raw_blocks[order - 1])
                if prev_text and (
                    len(prev_text) < 200
                    or prev_text.lower().startswith(("fig", "image", "table"))
                ):
                    caption_text = prev_text

            content = caption_text if caption_text else f"[Image on page {page_num}]"

        elif raw_block.get("type") == 0:  # Text block
            lines = raw_block.get("lines", [])
            if isinstance(lines, list):
                content_lines = []
                for line in lines:
                    if not isinstance(line, dict):
                        continue

                    span_texts = []
                    for span in line.get("spans", []):
                        if isinstance(span, dict):
                            span_texts.append(span.get("text", ""))
                            size = span.get("size", 0.0)
                            block_max_size = max(block_max_size, size)

                    content_lines.append("".join(span_texts))
                content = "\n".join(content_lines)

            pre_assigned = raw_block.get("pre_assigned_type")
            if pre_assigned:
                block_type = pre_assigned
                if block_type == "heading":
                    heading_level = 2
                elif block_type == "table":
                    confidence = 0.7
            else:
                # Determining if it is a heading based on font size threshold
                if block_max_size > (median_size + 1.5):
                    block_type = "heading"
                    if block_max_size > median_size + 6.0:
                        heading_level = 1
                    elif block_max_size > median_size + 3.0:
                        heading_level = 2
                    else:
                        heading_level = 3
                elif "|" in content and content.count("|") > 3:
                    block_type = "table"
                    confidence = 0.7
                elif block_type == "paragraph" and content.strip():
                    # Check for standard bullets or numbering patterns at the start of the block
                    list_pattern = r"^\s*([\u2022\u25E6\u25A0\*\-\·\▪]|\d+[\.\)])\s+"
                    if re.match(list_pattern, content):
                        block_type = "list"

            if block_type == "paragraph" and content.strip():
                content_upper = content.upper()
                is_short = len(content) < 100

                has_trademark = any(s in content for s in ["™", "®", "©"])
                is_toc = content_upper in ["CONTENTS", "TABLE OF CONTENTS"]
                # Catches short, all-caps lines usually found on the first few pages
                is_title_caps = is_short and content.isupper() and page_num <= 3

                if has_trademark or is_toc or is_title_caps:
                    block_type = "decorative"

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

    if hasattr(settings, "UNSTRUCTURED_API_KEY") and settings.UNSTRUCTURED_API_KEY:
        headers["unstructured-api-key"] = settings.UNSTRUCTURED_API_KEY

    try:
        response = requests.post(
            api_url,
            headers=headers,
            files={
                "files": ("rulebook.pdf", io.BytesIO(file_bytes), "application/pdf")
            },
            data={
                "strategy": "hi_res",
                "pdf_infer_table_structure": "true",
                "extract_image_block_to_payload": "true",
            },
            timeout=120,
        )
        response.raise_for_status()
        elements = response.json()

        pages = {}
        for el in elements:
            page_num = el.get("metadata", {}).get("page_number", 1)

            if page_num not in pages:
                pages[page_num] = {
                    "text": "",
                    "blocks": [],
                    "metadata": {"page": page_num},
                }

            unstructured_type = el.get("type")
            mapped_type = 1 if unstructured_type == "Image" else 0

            type_mapping = {
                "Title": "heading",
                "Table": "table",
                "ListItem": "list",
                "Image": "image",
            }
            pre_assigned = type_mapping.get(unstructured_type, "paragraph")

            block_dict = {
                "type": mapped_type,
                "pre_assigned_type": pre_assigned,
                "lines": [{"spans": [{"text": el.get("text", "")}]}],
            }

            if mapped_type == 1:
                b64_image = el.get("metadata", {}).get("image_base64")
                if b64_image:
                    try:
                        block_dict["image"] = base64.b64decode(b64_image)
                        block_dict["ext"] = "jpeg"
                    except Exception as e:
                        logger.warning(
                            f"Failed to decode Tier-3 image payload on page {page_num}: {e}"
                        )

            pages[page_num]["blocks"].append(block_dict)

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
