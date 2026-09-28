import json
import logging
import math
import re
from collections import defaultdict
from typing import Any, cast

import pymupdf
import pymupdf4llm

from app.config import settings
from app.ingestion import vlm
from app.ingestion.enums.lm_enums import LmStatus
from app.services.r2_service import upload_to_r2

logger = logging.getLogger(__name__)

_MIXED_UP_CHAR_PATTERN = re.compile(r"[^\x00-\x7F\xA0-\xFF]")

MIN_CHARS_PER_PAGE = 100
MAX_MIXED_UP_CHAR_RATIO = 0.15
MIN_TEXT_DENSITY = 0.06
MAX_WIDTH_COEFFICIENT_OF_VARIATION = 0.9
MIN_TEXT_BLOCKS_FOR_DENSITY_CHECK = 4
MIN_TEXT_BLOCKS_FOR_WIDTH_CHECK = 6

PENDING_REVIEW_CHAR_RATIO_THRESHOLD = 0.30
PENDING_REVIEW_PAGE_RATIO_THRESHOLD = 0.25

_DIGIT_RUN_PATTERN = re.compile(r"\d+")
LINE_MIN_PAGE_COUNT = 3
LINE_MIN_LENGTH = 4
LINE_OCCURRENCE_RATIO = 0.4


def extract_text(file_bytes: bytes, rulebook_id: str) -> tuple[bool, str, str, dict]:
    """
    Extracts Markdown from a PDF using pymupdf4llm.
    Escalates individual low-quality pages to Gemini VLM.
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

            pages = _build_initial_pages(md_dicts)
            _escalate_low_quality_pages(pages, pdf_document)
            _strip_running_headers_footers(pages)

            pages_cache = {
                "rulebookId": rulebook_id,
                "extractorVersion": "markdown-vlm-v1",
                "pages": pages,
                "pageQuality": [
                    {
                        "page": p["page"],
                        "escalated": p["escalated"],
                        "flagged": p["flagged"],
                        "reason": p["reason"],
                    }
                    for p in pages
                ],
            }
            pages_cache["qualitySummary"] = _summarize_extraction_quality(pages)

            flat_text = "\n\n".join(
                [p["markdown"] for p in pages if p["markdown"].strip()]
            )

            cache_key = f"rulebooks/{rulebook_id}/pages_v1.json"
            upload_to_r2(
                json.dumps(pages_cache).encode("utf-8"), cache_key, "application/json"
            )

            return (True, flat_text.strip(), "", pages_cache)
    except Exception:
        logger.exception("Extraction failed")
        return (False, "", "Internal error occurred during text extraction.", {})


def _build_initial_pages(md_dicts: list[dict[str, Any]]) -> list[dict]:
    """Wraps pymupdf4llm's per-page output in the custom page schema."""
    pages = []
    for page_num, page_dict in enumerate(md_dicts):
        if not isinstance(page_dict, dict):
            continue

        metadata = page_dict.get("metadata", {})
        page_number = (
            metadata.get("page", page_num + 1)
            if isinstance(metadata, dict)
            else page_num + 1
        )

        pages.append(
            {
                "page": page_number,
                "markdown": page_dict.get("text", ""),
                "confidence": 1.0,
                "escalated": False,
                "flagged": False,
                "reason": "",
            }
        )
    return pages


def _escalate_low_quality_pages(
    pages: list[dict], pdf_document: "pymupdf.Document"
) -> None:
    """
    Mutates 'pages' in place: flagged pages are sent to the VLM or are marked for review if VLM call fails
    """
    vlm_available = bool(getattr(settings, "GEMINI_API_KEY", None))
    gemini_cap_exhausted = False

    for page_entry in pages:
        page_index = page_entry["page"] - 1
        if not (0 <= page_index < len(pdf_document)):
            continue

        needs_escalation, reason = _assess_page(
            pdf_document, page_index, page_entry["markdown"]
        )
        if not needs_escalation:
            continue

        text, source, confidence, gemini_cap_exhausted = _attempt_escalation(
            pdf_document, page_index, vlm_available, gemini_cap_exhausted
        )
        _update_page_entry(page_entry, text, reason, source, confidence)


def _attempt_escalation(
    pdf_document: "pymupdf.Document",
    page_index: int,
    vlm_available: bool,
    gemini_cap_exhausted: bool,
) -> tuple[str | None, str, float, bool]:
    text: str | None = None
    source = "failed_unknown"
    confidence = 0.5

    if vlm_available and not gemini_cap_exhausted:
        text, source, confidence, gemini_cap_exhausted = _try_gemini_vlm(
            pdf_document, page_index, gemini_cap_exhausted
        )

    if text is None:
        text, source, confidence = _try_fallback_glm(pdf_document, page_index)

    return (text, source, confidence, gemini_cap_exhausted)


def _try_gemini_vlm(
    pdf_document: "pymupdf.Document", page_index: int, cap_exhausted: bool
) -> tuple[str | None, str, float, bool]:
    vlm_text, vlm_status = vlm.extract_page_via_vlm(pdf_document, page_index)

    if vlm_status == LmStatus.OK and vlm_text:
        return (vlm_text, "vlm_ok", 0.9, cap_exhausted)
    elif vlm_status == LmStatus.MAX_TOKENS and vlm_text:
        logger.warning(
            "VLM hit MAX_TOKENS on page %d. Keeping partial output.",
            page_index + 1,
        )
        return (vlm_text, "vlm_max_tokens", 0.75, cap_exhausted)
    elif vlm_status == LmStatus.DAILY_CAP_EXHAUSTED:
        logger.warning(
            "VLM daily cap exhausted at page %d. Routing the rest of the rulebook to the fallback.",
            page_index + 1,
        )
        return (None, "failed_cap_exhausted", 0.5, True)

    if vlm_status in (LmStatus.RECITATION, LmStatus.SAFETY):
        logger.info(
            "VLM refused page %d (%s). Trying fallback",
            page_index + 1,
            vlm_status.value,
        )
    else:
        logger.info(
            "VLM failed page %d (%s). Trying fallback",
            page_index + 1,
            vlm_status.value,
        )
    return (None, f"failed_{vlm_status.value.lower()}", 0.5, cap_exhausted)


def _try_fallback_glm(
    pdf_document: "pymupdf.Document", page_index: int
) -> tuple[str | None, str, float]:
    glm_text, glm_status = vlm.extract_page_via_glm(pdf_document, page_index)

    if glm_status == LmStatus.OK and glm_text:
        return (glm_text, "glm_ok", 0.85)
    elif glm_status == LmStatus.MAX_TOKENS and glm_text:
        logger.warning(
            "GLM hit MAX_TOKENS on page %d. Keeping partial output.",
            page_index + 1,
        )
        return (glm_text, "glm_max_tokens", 0.70)

    return (None, f"failed_{glm_status.value.lower()}", 0.5)


def _update_page_entry(
    page_entry: dict, text: str | None, reason: str, source: str, confidence: float
) -> None:
    if text and text.strip():
        page_entry["markdown"] = text
        page_entry["escalated"] = True
        page_entry["confidence"] = confidence
        page_entry["flagged"] = False
    else:
        page_entry["flagged"] = True
        page_entry["confidence"] = 0.5
    page_entry["reason"] = f"{reason}__{source}"


def _assess_page(
    pdf_document: "pymupdf.Document", page_index: int, markdown_text: str
) -> tuple[bool, str]:
    """
    Page-level signal for whether the markdown for the page is trustworthy.
    """
    stripped = markdown_text.strip()

    if len(stripped) < MIN_CHARS_PER_PAGE:
        return (True, "sparse_text_output")

    mixed_up_ratio = len(_MIXED_UP_CHAR_PATTERN.findall(stripped)) / len(stripped)
    if mixed_up_ratio > MAX_MIXED_UP_CHAR_RATIO:
        return (True, "high_mixed_up_character_ratio")

    try:
        raw_page = pdf_document[page_index]
        raw_dict = raw_page.get_text("dict")
    except Exception:
        logger.exception("Failed to read raw page dict to assess its quality.")
        return (False, "")

    raw_blocks = raw_dict.get("blocks", []) if isinstance(raw_dict, dict) else {}
    text_blocks = [
        b
        for b in raw_blocks
        if isinstance(b, dict) and b.get("type") == 0 and b.get("bbox")
    ]

    if not text_blocks:
        return (False, "")

    page_area = raw_page.rect.width * raw_page.rect.height
    if page_area <= 0:
        return (False, "")

    text_area = sum(
        max(0.0, (b["bbox"][2] - b["bbox"][0]) * (b["bbox"][3] - b["bbox"][1]))
        for b in text_blocks
    )
    text_density = text_area / page_area

    # Sparse text scattered over a uncovered page suggests that text is sitting ontop of an illustration/ art rather than a clean text layout
    if (
        text_density < MIN_TEXT_DENSITY
        and len(text_blocks) >= MIN_TEXT_BLOCKS_FOR_DENSITY_CHECK
    ):
        return (True, "low_text_density")

    widths = [b["bbox"][2] - b["bbox"][0] for b in text_blocks]
    mean_width = sum(widths) / len(widths)
    if mean_width > 0 and len(text_blocks) >= MIN_TEXT_BLOCKS_FOR_WIDTH_CHECK:
        width_variance = sum((w - mean_width) ** 2 for w in widths) / len(widths)
        width_cv = (width_variance**0.5) / mean_width

        # High variance in block widths suggests an icon grid, reference table, or sidebar-heavy page rather than clean flowing columns.
        if width_cv > MAX_WIDTH_COEFFICIENT_OF_VARIATION:
            return (True, "irregular_block_widths")

    return (False, "")


def _normalize_line_for_dedup(line: str) -> str:
    return _DIGIT_RUN_PATTERN.sub("#", line.strip().lower())


def _strip_running_headers_footers(pages: list[dict]) -> None:
    """
    Detects lines that recur near-identically across a
    large fraction of pages and strips then from every page.
    """
    if len(pages) < LINE_MIN_PAGE_COUNT:
        return

    line_page_counts: dict[str, set[int]] = defaultdict(set)

    for page in pages:
        seen_this_page = set()
        for raw_line in page["markdown"].splitlines():
            stripped = raw_line.strip()
            if len(stripped) < LINE_MIN_LENGTH:
                continue
            norm = _normalize_line_for_dedup(stripped)
            if norm in seen_this_page:
                continue
            seen_this_page.add(norm)
            line_page_counts[norm].add(page["page"])

    threshold = max(LINE_MIN_PAGE_COUNT, math.ceil(len(pages) * LINE_OCCURRENCE_RATIO))
    noise_norms = {
        norm for norm, pgs in line_page_counts.items() if len(pgs) >= threshold
    }
    if not noise_norms:
        return

    for page in pages:
        kept_lines = [
            raw_line
            for raw_line in page["markdown"].splitlines()
            if not raw_line.strip()
            or _normalize_line_for_dedup(raw_line.strip()) not in noise_norms
        ]
        page["markdown"] = "\n".join(kept_lines)


def _summarize_extraction_quality(pages: list[dict]) -> dict:
    """
    Produces the final extraction verdict: 'ready' or 'pending_review'
    """
    total_chars = sum(len(p["markdown"]) for p in pages)
    total_pages = len(pages)
    flagged = [p for p in pages if p["flagged"]]
    flagged_pages = [p["page"] for p in flagged]

    if total_chars == 0:
        return {
            "outcome": "pending_review",
            "reason": "No text content was extracted from any page.",
            "flaggedCharRatio": 1.0,
            "flaggedPageRatio": 1.0,
            "flaggedPages": [p["page"] for p in pages],
        }

    flagged_chars = sum(len(p["markdown"]) for p in pages if p["flagged"])
    flagged_char_ratio = flagged_chars / total_chars
    flagged_page_ratio = len(flagged) / total_pages

    if (
        flagged_char_ratio > PENDING_REVIEW_CHAR_RATIO_THRESHOLD
        or flagged_page_ratio > PENDING_REVIEW_PAGE_RATIO_THRESHOLD
    ):
        return {
            "outcome": "pending_review",
            "reason": (
                f"{len(flagged)}/{total_pages} pages flagged (pages: {flagged_pages})."
            ),
            "flaggedCharRatio": flagged_char_ratio,
            "flaggedPageRatio": flagged_page_ratio,
            "flaggedPages": flagged_pages,
        }

    return {
        "outcome": "ready",
        "reason": "",
        "flaggedCharRatio": flagged_char_ratio,
        "flaggedPageRatio": flagged_page_ratio,
        "flaggedPages": flagged_pages,
    }
