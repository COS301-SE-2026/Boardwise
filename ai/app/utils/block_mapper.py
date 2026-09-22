import hashlib
import re
import uuid
from typing import Any

from app.config import settings


def map_to_blocks(
    page_dict: dict[str, Any],
    rulebook_id: str,
    pending_uploads: list[tuple[bytes, str, str]],
    seen_image_hashes: set[str],
    image_base_url: str,
    confidence_override: float | None = None,
    force_review: bool = False,
) -> list[dict]:
    """Transforms a page dictionary into the block-level schema"""
    blocks = []
    metadata = page_dict.get("metadata", {})
    page_num = metadata.get("page", 1) if isinstance(metadata, dict) else 1

    raw_blocks = page_dict.get("blocks", [])
    if not isinstance(raw_blocks, list):
        raw_blocks = []

    median_size = _calculate_median_font_size(raw_blocks)

    for order, raw_block in enumerate(raw_blocks):
        if not isinstance(raw_block, dict):
            continue

        block_id = f"blk_{uuid.uuid4().hex[:8]}"
        raw_type = raw_block.get("type")

        if raw_type == 1:  # Image block in pymupdf
            block_data = _process_image_block(
                raw_block,
                order,
                raw_blocks,
                page_num,
                rulebook_id,
                seen_image_hashes,
                pending_uploads,
                image_base_url,
            )
            if not block_data:
                continue
        elif raw_type == 0:  # Text block
            block_data = _process_text_block(raw_block, page_num, median_size)
            if not block_data or (
                _is_ocr_gibberish(block_data["content"])
                and block_data["type"] not in ("decorative", "table")
            ):
                continue
        else:
            continue

        final_confidence = block_data["confidence"]
        if confidence_override is not None:
            final_confidence = min(final_confidence, confidence_override)

        blocks.append(
            {
                "blockId": block_id,
                "order": order,
                "type": block_data["type"],
                "content": block_data["content"],
                "confidence": final_confidence,
                "forceReview": force_review,
                "sourcePage": page_num,
                "imageUrl": block_data.get("image_url"),
                "headingLevel": block_data.get("heading_level"),
            }
        )

    return blocks


def _calculate_median_font_size(raw_blocks: list[dict]) -> float:
    """Calculates the baseline font size for the page to identify headings."""
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
        return font_sizes[len(font_sizes) // 2]

    return 11.0  # Fallback


def _process_image_block(
    raw_block: dict,
    order: int,
    raw_blocks: list[dict],
    page_num: int,
    rulebook_id: str,
    seen_image_hashes: set[str],
    pending_uploads: list[tuple[bytes, str, str]],
    image_base_url: str,
) -> dict | None:
    """Handles dimension validation, deduplication, and caption extraction for images."""
    bbox = raw_block.get("bbox")
    if bbox and len(bbox) == 4:
        bw = bbox[2] - bbox[0]
        bh = bbox[3] - bbox[1]
        if bw < settings.MIN_IMAGE_DIMENSION_PX or bh < settings.MIN_IMAGE_DIMENSION_PX:
            # Too small to be a meaningful diagram/ illustration; skip it
            return None

    image_bytes = raw_block.get("image")
    image_ext = raw_block.get("ext", "png")
    image_hash = hashlib.sha256(image_bytes).hexdigest() if image_bytes else None

    if image_hash:
        if image_hash in seen_image_hashes:
            return None
        seen_image_hashes.add(image_hash)

    image_key = f"rulebooks/{rulebook_id}/images/{page_num}_{order}.{image_ext}"
    image_url = f"{image_base_url}{image_key}"

    if image_bytes:
        pending_uploads.append((image_bytes, image_key, f"image/{image_ext}"))

    caption_text = _find_image_caption(order, raw_blocks)
    alt_text = caption_text if caption_text else f"Rulebook image, page {page_num}"

    return {
        "type": "image",
        "content": f"![{alt_text}]({image_url})",  # Rendering as markdown image syntax
        "confidence": 0.95,
        "image_url": image_url,
        "heading_level": None,
    }


def _find_image_caption(order: int, raw_blocks: list[dict]) -> str:
    """Looks ahead and behind the image block to find associated captions."""
    if (order + 1) < len(raw_blocks):
        next_text = _extract_text_from_raw_block(raw_blocks[order + 1])
        # Checking if it looks like a caption (short text or is explicitly labeled)
        if next_text and (
            len(next_text) < 200
            or next_text.lower().startswith(("fig", "image", "table"))
        ):
            return next_text

    # Look behind (above) if nothing was found below
    if order - 1 >= 0:
        prev_text = _extract_text_from_raw_block(raw_blocks[order - 1])
        if prev_text and (
            len(prev_text) < 200
            or prev_text.lower().startswith(("fig", "image", "table"))
        ):
            return prev_text

    return ""


def _process_text_block(
    raw_block: dict, page_num: int, median_size: float
) -> dict | None:
    """Extracts text content, determines heading levels, and classifies the text block."""
    content_lines = []
    block_max_size = 0.0

    for line in raw_block.get("lines", []):
        if not isinstance(line, dict):
            continue

        span_texts = []
        for span in line.get("spans", []):
            if isinstance(span, dict):
                span_texts.append(span.get("text", ""))
                size = span.get("size", 0.0)
                block_max_size = max(block_max_size, size)

        content_lines.append("".join(span_texts))

    content = "\n".join(content_lines).strip()
    if not content:
        return None

    block_type = "paragraph"
    confidence = 1.0
    heading_level = None
    pre_assigned = raw_block.get("pre_assigned_type")

    if pre_assigned:
        block_type = pre_assigned
        if block_type == "heading":
            heading_level = 2
        elif block_type == "table":
            confidence = 0.7
        elif block_type == "aside":
            confidence = 0.9
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
        else:
            # Check for standard bullets or numbering patterns at the start of the block
            list_pattern = r"^\s*([\u2022\u25E6\u25A0\*\-\·\▪]|\d+[\.\)])\s+"
            if re.match(list_pattern, content):
                block_type = "list"

    if block_type == "paragraph":
        has_trademark = any(s in content for s in ["™", "®", "©"])
        is_toc = content.upper() in ["CONTENTS", "TABLE OF CONTENTS"]
        # Catches short, all-caps lines usually found on the first few pages
        is_title_caps = (len(content) < 100) and content.isupper() and page_num <= 3
        if has_trademark or is_toc or is_title_caps:
            block_type = "decorative"

    return {
        "type": block_type,
        "content": content,
        "confidence": confidence,
        "heading_level": heading_level,
    }


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
