import logging
import random
import time

import httpx
from google.genai.errors import APIError

from app.ingestion.enums.lm_enums import LmStatus

logger = logging.getLogger(__name__)

TRANSIENT_STATUS_CODES = {429, 500, 502, 503, 504}


def handle_gemini_api_error(
    e: APIError, attempt: int, max_retries: int, *, label: str, base_retry_delay: float
) -> tuple[bool, LmStatus]:
    """Helps process API errors and determine if a retry can be done"""
    if e.code not in TRANSIENT_STATUS_CODES:
        logger.exception("%s returned error on which we cannot retry: %s", label, e)
        return (False, LmStatus.API_ERROR)

    if e.code == 429:
        error_str = str(e).lower()
        if "quota" in error_str or "resource_exhausted" in error_str:
            return (False, LmStatus.DAILY_CAP_EXHAUSTED)

    logger.warning(
        "%s returned %s. Attempt %d of %d.", label, e.code, attempt + 1, max_retries
    )

    if attempt < max_retries - 1:
        delay = (base_retry_delay * (2**attempt)) + random.uniform(0, 0.5)
        time.sleep(delay)
        return (True, LmStatus.OK)

    logger.exception("%s exhausted retries for status %s", label, e.code)
    return (False, LmStatus.RETRIES_EXHAUSTED)


def is_transient_glm_error(exc: Exception) -> bool:
    code = (
        getattr(exc, "code", None)
        or getattr(exc, "status_code", None)
        or getattr(exc, "http_status", None)
    )
    if isinstance(code, int) and code in TRANSIENT_STATUS_CODES:
        return True

    if isinstance(exc, (httpx.TimeoutException, TimeoutError)):
        return True

    msg = str(exc).lower()
    if "timeout" in msg or "timed out" in msg:
        return True

    return any(f" {c} " in msg or f" {c}," in msg for c in TRANSIENT_STATUS_CODES)
