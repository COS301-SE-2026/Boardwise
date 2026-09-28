import base64
import logging
import random
import time

import httpx
import pymupdf
from google import genai
from google.genai import types
from google.genai.errors import APIError
from zhipuai import ZhipuAI

from app.config import settings
from app.ingestion.enums.lm_enums import LmStatus
from app.ingestion.utils.api_retry_utils import (
    handle_gemini_api_error,
    is_transient_glm_error,
)

logger = logging.getLogger(__name__)

GEMINI_MODEL = "gemini-3.5-flash-lite"

GLM_MODEL = "glm-4.6v-flash"

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
7. If an area contains only decorative artwork or background texture and no readable text, ignore it without comment.
8. Do not summarize, reword, or add your own notes. Include only what is visibly present on the page. If something is truly unreadable, use [illegible] instead of guessing.
9. Return only the Markdown transcription for this page—no introduction, no explanation, and no code fences surrounding the output.
"""

_client: genai.Client | None = None
_glm_client = (
    ZhipuAI(api_key=settings.GLM_API_KEY)
    if getattr(settings, "GLM_API_KEY", None)
    else None
)


def _get_gemini_client() -> genai.Client | None:
    global _client
    if _client is not None:
        return _client
    if not getattr(settings, "GEMINI_API_KEY", None):
        return None
    http_client = httpx.Client(
        timeout=httpx.Timeout(
            connect=15.0,
            read=VLM_REQUEST_TIMEOUT_SECONDS,
            write=30.0,
            pool=15.0,
        ),
    )

    _client = genai.Client(
        api_key=settings.GEMINI_API_KEY,
        http_options=types.HttpOptions(httpx_client=http_client),
    )
    return _client


def _render_page_to_png(pdf_document: "pymupdf.Document", page_num: int) -> bytes:
    """Rasterises a page for the vision model."""
    page = pdf_document[page_num]
    pix = page.get_pixmap(dpi=VLM_RENDER_DPI)
    return pix.tobytes("png")


def _extract_text_from_response(response) -> tuple[str | None, LmStatus]:
    """Inspect a Gemini response and maps finish_reason to a LmStatus."""
    if not response.candidates:
        logger.warning(
            "Gemini returned no candidates. prompt_feedback=%s",
            getattr(response, "prompt_feedback", None),
        )
        return (None, LmStatus.EMPTY)

    cand = response.candidates[0]
    finish_reason = getattr(cand, "finish_reason", None)
    reason_name = "UNKNOWN"
    if finish_reason:
        if hasattr(finish_reason, "name"):
            reason_name = finish_reason.name
        else:
            reason_name = str(finish_reason)

    parts = cand.content.parts if cand.content and cand.content.parts else []
    text_parts = [p.text for p in parts if getattr(p, "text", None)]
    combined = "".join(text_parts).strip()

    reason_upper = reason_name.upper()
    if "RECITATION" in reason_upper:
        return (None, LmStatus.RECITATION)
    if "SAFETY" in reason_upper:
        return (None, LmStatus.SAFETY)
    if "MAX_TOKENS" in reason_upper:
        logger.warning(
            "Gemini hit MAX_TOKENS. (usage=%s)",
            getattr(response, "usage_metadata", None),
        )
        return (combined or None, LmStatus.MAX_TOKENS)

    if not combined:
        logger.warning(
            "Gemini returned no text. finish_reason=%s usage=%s",
            reason_name,
            getattr(response, "usage_metadata", None),
        )
        return (None, LmStatus.EMPTY)

    if reason_upper not in ("STOP", "FINISH_REASON_STOP", "UNKNOWN"):
        logger.warning(
            "Gemini finished with reason=%s but produced %d chars.",
            reason_name,
            len(combined),
        )
    return (combined, LmStatus.OK)


def _extract_text_from_glm_response(response) -> tuple[str | None, LmStatus]:
    """Inspect a GLM response and maps finish_reason to a LmStatus."""
    if not response.choices:
        return (None, LmStatus.EMPTY)

    choice = response.choices[0]
    finish_reason = (choice.finish_reason or "").lower()
    text = (choice.message.content or "").strip() if choice.message else ""

    if finish_reason == "content_filter":
        return (None, LmStatus.SAFETY)
    if finish_reason == "length":
        logger.warning("GLM hit max_token for this page.")
        return (text or None, LmStatus.MAX_TOKENS)

    if not text:
        return (None, LmStatus.EMPTY)
    return (text, LmStatus.OK)


def _call_vlm(image_bytes: bytes, max_retries: int = 3) -> tuple[str | None, LmStatus]:
    """
    Sends a single page image to the VLM for transcription.
    Returns (markdown text, or None, LmStatus).
    """
    client = _get_gemini_client()
    if client is None:
        logger.error("GEMINI_API_KEY is not configured. Cannot escalate to VLM.")
        return (None, LmStatus.NOT_CONFIGURED)

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
            return _extract_text_from_response(response)

        except APIError as e:
            should_retry, status = handle_gemini_api_error(
                e,
                attempt,
                max_retries,
                label="VLM",
                base_retry_delay=VLM_BASE_RETRY_DELAY_SECONDS,
            )
            if should_retry:
                continue
            return (None, status)

        except ValueError:
            logger.exception("VLM produced empty or blocked response.")
            return (None, LmStatus.EMPTY)

        except Exception:
            logger.exception("Unexpected error during VLM call.")
            return (None, LmStatus.API_ERROR)

    return (None, LmStatus.RETRIES_EXHAUSTED)


def _call_glm(
    image_bytes: bytes, page_num: int, max_retries: int = 3
) -> tuple[str | None, LmStatus]:
    """
    Sends a single page image to the fallback GLM for transcription.
    Returns (markdown text | None, LmStatus).
    """
    if _glm_client is None:
        return (None, LmStatus.NOT_CONFIGURED)

    encoded = base64.b64encode(image_bytes).decode("utf-8")

    for attempt in range(max_retries):
        try:
            response = _glm_client.chat.completions.create(
                model=GLM_MODEL,
                messages=[
                    {
                        "role": "user",
                        "content": [
                            {"type": "text", "text": TRANSCRIPTION_PROMPT},
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": f"data:image/png;base64,{encoded}"
                                },
                            },
                        ],
                    }
                ],
                temperature=0.1,
                max_tokens=VLM_MAX_OUTPUT_TOKENS,
                stream=False,
                timeout=VLM_REQUEST_TIMEOUT_SECONDS,
            )

            text, status = _extract_text_from_glm_response(response)
            if status in (
                LmStatus.OK,
                LmStatus.RECITATION,
                LmStatus.SAFETY,
                LmStatus.EMPTY,
                LmStatus.MAX_TOKENS,
            ):
                return (text, status)

            if attempt < max_retries - 1:
                delay = (VLM_BASE_RETRY_DELAY_SECONDS * (2**attempt)) + random.uniform(
                    0, 0.5
                )
                time.sleep(delay)
                continue
            return (None, status)
        except Exception as e:
            if is_transient_glm_error(e) and attempt < max_retries - 1:
                delay = (VLM_BASE_RETRY_DELAY_SECONDS * (2**attempt)) + random.uniform(
                    0, 0.5
                )
                time.sleep(delay)
                continue
            logger.error("GLM failed for page %d (attempt %d)", page_num, attempt + 1)
            return (None, LmStatus.API_ERROR)
    return (None, LmStatus.RETRIES_EXHAUSTED)


def extract_page_via_vlm(
    pdf_document: "pymupdf.Document", page_num: int, max_retries: int = 3
) -> tuple[str | None, LmStatus]:
    """
    Renders a single page into an image and sends it to the VLM for transcription
    Returns markdown text for the page or None on failure
    """
    try:
        image_bytes = _render_page_to_png(pdf_document, page_num)
    except Exception:
        logger.exception("Failed to render page %d for VLM transcription", page_num)
        return (None, LmStatus.RENDER_ERROR)

    return _call_vlm(image_bytes, max_retries=max_retries)


def extract_page_via_glm(
    pdf_document: "pymupdf.Document", page_num: int, max_retries: int = 3
) -> tuple[str | None, LmStatus]:
    """
    Fallback transcription via Zhipu's GLM-4.6V-Flash
    Returns (markdown_text | None, status_tag)
    """
    if _glm_client is None:
        logger.error("GLM_API_KEY not configured")
        return (None, LmStatus.NOT_CONFIGURED)

    try:
        image_bytes = _render_page_to_png(pdf_document, page_num)
    except Exception:
        logger.exception("Failed to render page %d for GLM fallback", page_num)
        return (None, LmStatus.RENDER_ERROR)

    return _call_glm(image_bytes, page_num, max_retries=max_retries)
