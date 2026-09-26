import base64
import io
import json
import logging
import math
import re
from typing import Any, cast

import pymupdf
import pymupdf4llm
import requests

from app.config import settings
from app.services.r2_service import upload_to_r2
from app.utils import block_mapper

logger = logging.getLogger(__name__)


def _assess_and_refine_page(
    page_num: int,
    page_dict: dict,
    pdf_document: "pymupdf.Document",
    file_bytes: bytes,
    used_tier3: bool,
) -> tuple[dict, bool, str, float | None]:
    """Evaluates page quality, triggers escalations, and refines layout properties."""
    if not used_tier3:
        raw_page = pdf_document[page_num]
        raw_dict = raw_page.get_text("dict")
        raw_blocks_list = (
            raw_dict.get("blocks", []) if isinstance(raw_dict, dict) else []
        )

        escalate_page, escalation_reason = _assess_page(raw_page, raw_blocks_list)

        escalated_page_dict = None
        if escalate_page and settings.UNSTRUCTURED_API_URL:
            escalated_page_dict = _extract_single_page_via_tier3(file_bytes, page_num)

        if escalated_page_dict is not None:
            page_dict = escalated_page_dict
            page_dict["metadata"] = {"page": page_num + 1}
            return (page_dict, True, escalation_reason, 0.9)

        boxed_regions = _detect_boxed_regions(raw_page)
        _tag_boxed_blocks(raw_blocks_list, boxed_regions)
        page_dict["blocks"] = _reorder_blocks_by_column(raw_blocks_list, raw_page)
        return (page_dict, False, escalation_reason, (0.6 if escalate_page else None))

    page_blocks = page_dict.get("blocks", [])
    page_has_issues = any(
        b.get("type") == 0
        and (
            not any(
                span.get("text", "").strip()
                for line in b.get("lines", [])
                for span in line.get("spans", [])
            )
        )
        for b in page_blocks
        if isinstance(b, dict)
    )

    escalation_reason = (
        "tier3_fallback_flagged" if page_has_issues else "tier3_fallback"
    )
    return (
        page_dict,
        page_has_issues,
        escalation_reason,
        (0.6 if page_has_issues else None),
    )


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

            page_quality: list[dict] = []

            for page_num, page_dict in enumerate(md_dicts):
                page_dict, escalate_page, escalation_reason, confidence_override = (
                    _assess_and_refine_page(
                        page_num, page_dict, pdf_document, file_bytes, used_tier3
                    )
                )

                page_quality.append(
                    {
                        "page": page_num + 1,
                        "escalated": used_tier3
                        or (
                            confidence_override is not None
                            and math.isclose(confidence_override, 0.9)
                        ),
                        "flagged": escalate_page,
                        "reason": escalation_reason,
                    }
                )
                blocks_cache["blocks"].extend(
                    block_mapper.map_to_blocks(
                        page_dict=page_dict,
                        rulebook_id=rulebook_id,
                        confidence_override=confidence_override,
                        force_review=escalate_page,
                    )
                )

            flat_text_pieces = [
                str(b.get("content"))
                for b in blocks_cache["blocks"]
                if b.get("content")
            ]
            flat_text = "\n".join(flat_text_pieces)

            blocks_cache["pageQuality"] = page_quality
            blocks_cache["qualitySummary"] = _summarize_extraction_quality(
                blocks_cache, page_quality
            )

            cache_key = f"rulebooks/{rulebook_id}/blocks_v1.json"
            upload_to_r2(
                json.dumps(blocks_cache).encode("utf-8"), cache_key, "application/json"
            )

            return (True, flat_text.strip(), "", blocks_cache)
    except Exception:
        logger.exception("Extraction failed")
        return (False, "", "Internal error occurred during text extraction.", {})


def _reorder_blocks_by_column(
    raw_blocks: list[dict], page: "pymupdf.Page"
) -> list[dict]:
    """Reorders PyMuPDF's raw text/image blocks into visual reading order"""
    blocks = [b for b in raw_blocks if isinstance(b, dict) and b.get("bbox")]
    if not blocks:
        return raw_blocks

    try:
        from pymupdf4llm.helpers.multi_column import column_boxes

        columns = column_boxes(page, footer_margin=40, no_image_text=False)
    except Exception:
        logger.exception(
            "column_boxes unavailable or failed; using fallback column heuristic."
        )
        return _reorder_blocks_by_column_fallback(raw_blocks, page.rect.width)

    if not columns:
        return sorted(blocks, key=lambda b: (b["bbox"][1], b["bbox"][0]))

    def column_index(b: dict) -> int:
        x0, y0, x1, y1 = b["bbox"]
        cx = (x0 + x1) / 2
        cy = (y0 + y1) / 2
        for i, col_rect in enumerate(columns):
            if col_rect.x0 <= cx <= col_rect.x1 and col_rect.y0 <= cy <= col_rect.y1:
                return i

        # If a block falls ouside every detected column, it will be placed on the x-axis by nearest column center such that it is not lost.
        return min(
            range(len(columns)),
            key=lambda i: abs(((columns[i].x0 + columns[i].x1) / 2) - cx),
        )

    return sorted(blocks, key=lambda b: (column_index(b), b["bbox"][1], b["bbox"][0]))


def _create_fallback_bands(
    blocks: list[dict], page_width: float
) -> list[tuple[str, list[dict]]]:
    """Groups blocks into full-width or column-based bands."""
    FULL_WIDTH_RATIO = 0.75
    bands: list[tuple[str, list[dict]]] = []
    current_band: list[dict] = []

    for b in blocks:
        x0, _, x1, _ = b["bbox"]
        if (x1 - x0) / page_width >= FULL_WIDTH_RATIO:
            if current_band:
                bands.append(("cols", current_band))
                current_band = []
            bands.append(("full", [b]))
        else:
            current_band.append(b)
    if current_band:
        bands.append(("cols", current_band))

    return bands


def _split_column_band(band_blocks: list[dict], page_width: float) -> list[dict]:
    """Splits a band of blocks into left and right columns if a significant gap exists."""
    GAP_MIN_FRACTION = 0.06
    x_centers = sorted((b["bbox"][0] + b["bbox"][2]) / 2 for b in band_blocks)
    boundary = None

    if len(x_centers) > 1:
        gaps = [
            (x_centers[i] - x_centers[i - 1], (x_centers[i] + x_centers[i - 1]) / 2)
            for i in range(1, len(x_centers))
        ]
        max_gap, midpoint = max(gaps, key=lambda g: g[0])
        if max_gap > page_width * GAP_MIN_FRACTION:
            boundary = midpoint
    if boundary is None:
        return sorted(band_blocks, key=lambda b: (b["bbox"][1], b["bbox"][0]))

    left_col = sorted(
        (b for b in band_blocks if (b["bbox"][0] + b["bbox"][2]) / 2 < boundary),
        key=lambda b: b["bbox"][1],
    )
    right_col = sorted(
        (b for b in band_blocks if (b["bbox"][0] + b["bbox"][2]) / 2 >= boundary),
        key=lambda b: b["bbox"][1],
    )

    return left_col + right_col


def _reorder_blocks_by_column_fallback(
    raw_blocks: list[dict], page_width: float
) -> list[dict]:
    """
    Fallback reading-order heuristic, used only if pymupdf4llm's column_boxes() is unavailable or raises an exception.
    Handles the common 2-column case of rulebooks.
    """
    blocks = [b for b in raw_blocks if isinstance(b, dict) and b.get("bbox")]
    if not blocks or page_width <= 0:
        return raw_blocks

    blocks = sorted(blocks, key=lambda b: b["bbox"][1])
    bands = _create_fallback_bands(blocks, page_width)

    ordered: list[dict] = []
    for kind, band_blocks in bands:
        if kind == "full":
            ordered.extend(band_blocks)
        else:
            ordered.extend(_split_column_band(band_blocks, page_width))

    return ordered


def _assess_page(raw_page: "pymupdf.Page", raw_blocks: list[dict]) -> tuple[bool, str]:
    """
    Run before reordering to flag pages that fail geometry-based column extraction
    such that they can be escalated to Tier-3 individually.
    Returns (needs_escalation, reason)
    """
    text_blocks = [
        b
        for b in raw_blocks
        if isinstance(b, dict) and b.get("type") == 0 and b.get("bbox")
    ]

    if not text_blocks:
        return (False, "")  # e.g. a full page diagram has nothing to reorder

    page_area = raw_page.rect.width * raw_page.rect.height
    if page_area <= 0:
        return (False, "")

    text_area = sum(
        max(0.0, (b["bbox"][2] - b["bbox"][0]) * (b["bbox"][3] - b["bbox"][1]))
        for b in text_blocks
    )

    text_density = text_area / page_area

    # Sparse text scattered over a uncovered page suggests that text is sitting ontop of an illustration/ art rather than a clean text layout
    if text_density < 0.06 and len(text_blocks) >= 4:
        return (True, "low_text_density")

    widths = [b["bbox"][2] - b["bbox"][0] for b in text_blocks]
    mean_width = sum(widths) / len(widths)
    if mean_width > 0:
        width_variance = sum((w - mean_width) ** 2 for w in widths) / len(widths)
        width_cv = (width_variance**0.5) / mean_width

        # High variance in block widths suggests an icon grid, reference table, or sidebar-heavy page rather than clean flowing columns.
        if width_cv > 0.9 and len(text_blocks) >= 6:
            return (True, "irregular_block_widths")

    return (False, "")


def _detect_boxed_regions(raw_page: "pymupdf.Page") -> list["pymupdf.Rect"]:
    """
    Detects filled/stroked rectangles on the page(callout boxes, sidebars, FAQ inserts)
    via PyMuPDF's vector drawing data such that their text is not spliced mid-sentence
    into the surrounding column flow during column reordering.
    """
    try:
        drawings = raw_page.get_drawings()
    except Exception:
        logger.exception("get_drawings() failed for boxed-region detection")
        return []

    regions = []
    page_area = raw_page.rect.width * raw_page.rect.height
    for d in drawings:
        rect = d.get("rect")
        if rect is None:
            continue

        area = rect.width * rect.height

        # Skip tiny shapes (bullets, underlines, table-cell, boarders) and anything close to a full-page (that would be a background and not a callout).
        if (
            area < settings.MIN_BOXED_REGION_AREA_PX
            or rect.height < settings.MIN_BOXED_REGION_HEIGHT_PT
            or (page_area > 0 and area / page_area > 0.85)
        ):
            continue

        if d.get("fill") is not None or d.get("color") is not None:
            regions.append(rect)

    return regions


def _tag_boxed_blocks(
    raw_blocks: list[dict], boxed_regions: list["pymupdf.Rect"]
) -> None:
    """
    Sets pre_assigned_type='aside' on text blocks whose bbox sits inside a detected boxed region.
    """
    if not boxed_regions:
        return

    for b in raw_blocks:
        if not isinstance(b, dict) or b.get("type") != 0 or not b.get("bbox"):
            continue

        x0, y0, x1, y1 = b["bbox"]
        block_area = max(0.0, (x1 - x0) * (y1 - y0))
        if block_area == 0:
            continue

        for region in boxed_regions:
            intersection = pymupdf.Rect(x0, y0, x1, y1) & region
            if intersection.is_empty:
                continue
            overlap_ratio = (intersection.width * intersection.height) / block_area
            if overlap_ratio > 0.7:
                b["pre_assigned_type"] = "aside"
                break


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


def _extract_single_page_via_tier3(file_bytes: bytes, page_num: int) -> dict | None:
    """
    Escalates a single difficult page to the Tier-3 Unstructured API
    Returns the escalated page's dict or None if isolation/ escalation failed
    """
    try:
        with pymupdf.open(stream=file_bytes, filetype="pdf") as src:
            single_page_doc = pymupdf.open()
            single_page_doc.insert_pdf(src, from_page=page_num, to_page=page_num)
            single_page_bytes = single_page_doc.tobytes()
            single_page_doc.close()
    except Exception:
        logger.exception("Failed to isolate page %d for Tier-3 escalation", page_num)
        return None

    pages = _escalate_to_tier_3(single_page_bytes)
    if not pages:
        return None

    return pages[0]


def _parse_tier3_element(el: dict, page_num: int) -> dict:
    """Parse a single structured element returned from the Tier 3 API."""
    unstructured_type = el.get("type")
    mapped_type = 1 if unstructured_type == "Image" else 0

    type_mapping: dict = {
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
    return block_dict


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
            timeout=300,
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

            pages[page_num]["blocks"].append(_parse_tier3_element(el, page_num))

        return [pages[p] for p in sorted(pages.keys())]
    except requests.exceptions.Timeout:
        logger.error(
            "Tier 3 extraction timed out. The local Docker container was too slow."
        )
    except requests.exceptions.ConnectionError:
        logger.error(
            "Tier 3 connection dropped. The Docker container likely hit its memory limit."
        )
    except requests.exceptions.HTTPError:
        logger.exception(
            "Tier 3 API returned an HTTP error(likely an OOM crash inside the container)"
        )
    except Exception:
        logger.exception("Unexpected error communicating with local Unstructured API.")

    return []


PENDING_REVIEW_CHAR_RATIO_THRESHOLD = 0.30


def _summarize_extraction_quality(blocks_cache: dict, page_quality: list[dict]) -> dict:
    """
    Produces the final extraction verdict: 'ready' or 'pending_review'
    """
    blocks = blocks_cache.get("blocks", [])
    scorable_blocks = [b for b in blocks if b.get("type") != "decorative"]

    total_chars = sum(len(b.get("content", "")) for b in scorable_blocks)
    if total_chars == 0:
        return {
            "outcome": "pending_review",
            "reason": "No scorable text content was extracted.",
            "flaggedCharRatio": 1.0,
            "flaggedPages": [pq["page"] for pq in page_quality if pq.get("flagged")],
        }

    flagged_chars = sum(
        len(b.get("content", ""))
        for b in scorable_blocks
        if b.get("forceReview") or b.get("confidence", 1.0) < 0.7
    )
    flagged_ratio = flagged_chars / total_chars
    flagged_pages = [pq["page"] for pq in page_quality if pq.get("flagged")]

    if flagged_ratio > PENDING_REVIEW_CHAR_RATIO_THRESHOLD:
        return {
            "outcome": "pending_review",
            "reason": (
                f"{flagged_ratio:.0%} of extracted content is low-confidence "
                f"or was flagged during extraction (pages: {flagged_pages})."
            ),
            "flaggedCharRatio": flagged_ratio,
            "flaggedPages": flagged_pages,
        }

    return {
        "outcome": "ready",
        "reason": "",
        "flaggedCharRatio": flagged_ratio,
        "flaggedPages": flagged_pages,
    }
