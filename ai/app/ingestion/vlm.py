"""Gemini Flash VLM escalation path for pages that fail the quality gate during text extraction."""

import base64
import logging
import random
import time

import pymupdf
from google import genai
from google.genai import types
from google.genai.errors import APIError

from app.config import settings

logger = logging.getLogger(__name__)

GEMINI_MODEL = "gemini-3.5-flash-lite"
GEMINI_API_URL = f"https://generativelanguage.googleapis.com/v1beta/models/{GEMINI_MODEL}:generateContent"

VLM_RENDER_DPI = 200
VLM_BASE_RETRY_DELAY_SECONDS = 2.0
VLM_MAX_OUTPUT_TOKENS = 8192
VLM_REQUEST_TIMEOUT_SECONDS = 60

TRANSCRIPTION_PROMPT = """Convert one page from a board-game rulebook into clean Markdown intended for a retrieval system. Follow these instructions precisely:
1. Copy every readable rules passage accurately, following the page's reading order: within a column, read left to right and top to bottom; complete the current column before moving to the next.
2. Represent section titles with Markdown headings (#, ##, ###), choosing the level that reflects their visual importance (the largest or boldest heading gets the top level).
3. Keep lists as Markdown bullets or numbered lists. Keep tables as GitHub-flavored Markdown tables, retaining all rows and columns.
4. Whenever the rules show a game icon instead of a word—such as a resource, action, or symbol—write it as a bracketed label that names what the icon shows, e.g. [Wood], [Gold Coin], [Move Action], [Combat Icon]. Never describe an icon in running prose; always use the bracketed form at the icon's position in the sentence.
5. For sidebars, "Example:" boxes, and flavor or thematic text that is visually set apart from the main rules, begin that block with "> " (a Markdown blockquote) so it can later be told apart from the core rules.
6. Leave out page numbers, running headers/footers, printer or production markings (file names, print/export dates, "Seite N", "Page N" folios), and copyright/trademark boilerplate. Do not transcribe them at all.
7.If an area contains only decorative artwork or background texture and no readable text, ignore it without comment.
8.Do not summarize, reword, or add your own notes. Include only what is visibly present on the page. If something is truly unreadable, use [illegible] instead of guessing.
9. Return only the Markdown transcription for this page—no introduction, no explanation, and no code fences surrounding the output.
"""

TRANSIENT_STATUS_CODES = {429, 500, 502, 503, 504}

client = genai.Client(api_key=settings.GEMINI_API_KEY)


def _render_page_to_png(pdf_document: "pymupdf.Document", page_num: int) -> bytes:
    """Rasterises a page for the vision model."""
    page = pdf_document[page_num]
    pix = page.get_pixmap(dpi=VLM_RENDER_DPI)
    return pix.tobytes("png")


def _build_payload(image_bytes: bytes) -> dict:
    encoded = base64.b64encode(image_bytes).decode("utf-8")
    return {
        "contents": [
            {
                "role": "user",
                "parts": [
                    {"text": TRANSCRIPTION_PROMPT},
                    {"inline_data": {"mime_type": "image/png", "data": encoded}},
                ],
            }
        ],
        "generationConfig": {
            "temperature": 0.1,
            "maxOutputTokens": VLM_MAX_OUTPUT_TOKENS,
        },
    }


def _extract_text_from_response(response_json: dict) -> str | None:
    try:
        candidates = response_json.get("candidates", [])
        if not candidates:
            logger.warning("VLM response contained no candidates.")
            return None

        finish_reason = candidates[0].get("finishReason")
        if finish_reason not in (None, "STOP"):
            logger.warning("VLM finished with reason=%s", finish_reason)

        parts = candidates[0].get("content", {}).get("parts", [])
        text_parts = [p.get("text", "") for p in parts if "text" in p]
        combined = "".join(text_parts).strip()
        return combined or None
    except (KeyError, IndexError, AttributeError, TypeError):
        logger.exception("Unexpected VLM response shape.")
        return None


def _call_vlm(image_bytes: bytes, max_retries: int = 3) -> str | None:
    """
    Sends a single page image to the VLM for transcription.
    Returns markdown text, or None if the call fails.
    """
    if not getattr(settings, "GEMINI_API_KEY", None):
        logger.error("GEMINI_API_KEY is not configured. Cannot escalate to VLM.")
        return None

    for attempt in range(max_retries):
        try:
            response = client.models.generate_content(
                model=GEMINI_MODEL,
                contents=[
                    TRANSCRIPTION_PROMPT,
                    types.Part.from_bytes(data=image_bytes, mime_type="image/png"),
                ],
                config=types.GenerateContentConfig(
                    temperature=0.1,
                    max_output_tokens=VLM_MAX_OUTPUT_TOKENS,
                    tools=[],
                    automatic_function_calling=types.AutomaticFunctionCallingConfig(
                        disable=True
                    ),
                ),
            )
            text = (response.text or "").strip()
            return text or None
        except APIError as e:
            if e.code in TRANSIENT_STATUS_CODES:
                logger.warning(
                    "VLM returned %s. Attempt %d of %d.",
                    e.code,
                    attempt + 1,
                    max_retries,
                )

                if attempt < max_retries - 1:
                    delay = (
                        VLM_BASE_RETRY_DELAY_SECONDS * (2**attempt)
                    ) + random.uniform(0, 0.5)
                    time.sleep(delay)
                    continue

                logger.error("VLM exhausted retries for status %s", e.code)
                return None

            logger.error("VLM returned error on which we cannot retry: %s", e)
            return None
        except ValueError:
            logger.exception("VLM produced empty or blocked response.")
            return None
        except Exception:
            logger.exception("Unexpected error during VLM call.")
            return None
    return None


def extract_page_via_vlm(
    pdf_document: "pymupdf.Document", page_num: int, max_retries: int = 3
) -> str | None:
    """
    Renders a single page into an image and sends it to the VLM for transcription
    Returns markdown text for the page or None on failure
    """
    try:
        image_bytes = _render_page_to_png(pdf_document, page_num)
    except Exception:
        logger.exception("Failed to render page %d for VLM transcription", page_num)
        return None

    return _call_vlm(image_bytes, max_retries=max_retries)
