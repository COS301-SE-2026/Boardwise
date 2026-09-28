import logging
import re
from typing import Any

from bson import ObjectId
from llama_cpp import Llama

from app.ingestion.language_models.llm_client import call_structured_llm
from app.ingestion.schemas.component_schemas import COMPONENT_EXTRACTION_SCHEMA
from app.utils.logging_utils import sanitise_log_input

logger = logging.getLogger(__name__)

_COMPONENT_HEADER_PATTERN = re.compile(
    r"(components?|contents?|materials?|what'?s in the box|game contents| box contents| setup)",
    re.IGNORECASE,
)

FALLBACK_CHUNK_SCAN_LIMIT = 5

MAX_CANDIDATE_CHARS = 6000

ALLOWED_COMPONENT_TYPES = frozenset(
    {"board", "card", "die", "token", "meeple", "tile", "miniature", "other"}
)
MIN_QUANTITY = 1
MAX_REASONABLE_QUANTITY = 500
MIN_NAME_CHARS = 2

_EXTRACTION_PROMPT = """\
Extract the physical component inventory from this board game rulebook excerpt.
List each distinct component and its stated quantity. Do not infer unlisted components.
Exclude rules text; output only the component list.

Rulebook excerpt:
---
{text}
---
"""


def extract_components(
    chunk_list: list[dict], rulebook_id: str, local_model: Llama | None = None
) -> tuple[bool, list[dict], str]:
    """
    Extracts the component list for a rulebook
    Returns:(success, component_list, failure_reason)
    """
    if not chunk_list:
        return (False, [], "No chunks available for component extraction")

    candidate_text = _select_candidate_text(chunk_list)
    if not candidate_text.strip():
        logger.warning(
            "No component-section candidate text found for rulebook %s",
            sanitise_log_input(rulebook_id),
        )
        return (False, [], "Could not locate a components section in the rulebook.")

    try:
        llm_ok, raw_components, llm_reason = _call_llm_for_components(
            candidate_text, local_model
        )
    except Exception:
        logger.exception(
            "Component extraction LLM call failed for rulenook %s",
            sanitise_log_input(rulebook_id),
        )
        return (False, [], "Internal error during component extraction")

    if not llm_ok:
        logger.warning(
            "Component extraction LLM call failed for rulenook %s: %s",
            sanitise_log_input(rulebook_id),
            llm_reason,
        )
        return (False, [], llm_reason)

    if not raw_components:
        return (False, [], "LLM returned no components for this rulebook")

    components = [_finalise_component(rulebook_id, raw) for raw in raw_components]
    components = _merge_duplicate_components(components)

    flagged_count = sum(1 for c in components if c["needsReview"])
    if flagged_count:
        logger.warning(
            "%d/%d extracted components flagged for review (rulebook %s)",
            flagged_count,
            len(components),
            sanitise_log_input(rulebook_id),
        )

    logger.warning(
        "Extracted %d components for rulebook %s",
        len(components),
        sanitise_log_input(rulebook_id),
    )

    return (True, components, "")


def _select_candidate_text(chunk_list: list[dict]) -> str:
    """
    Prefers chunks whose header breadcrumb matches a component-section keyword
    """
    matched = [
        chunk
        for chunk in chunk_list
        if _matches_component_header(chunk.get("metadata", {}))
    ]

    if matched:
        text = "\n\n".join(c["content"] for c in matched)
    else:
        fallback = sorted(chunk_list, key=lambda c: c.get("index", 0))[
            :FALLBACK_CHUNK_SCAN_LIMIT
        ]
        text = "\n\n".join(c["content"] for c in fallback)

    return text[:MAX_CANDIDATE_CHARS]


def _matches_component_header(metadata: dict[str, str]) -> bool:
    return any(
        isinstance(v, str) and _COMPONENT_HEADER_PATTERN.search(v)
        for v in metadata.values()
    )


def _call_llm_for_components(
    candidate_text: str, local_model: Llama | None = None
) -> tuple[bool, list[dict[str, Any]], str]:
    """Calls text LLM with a JSON schema so output is valid"""
    prompt = _EXTRACTION_PROMPT.format(text=candidate_text)
    success, parsed, reason = call_structured_llm(
        prompt=prompt, schema=COMPONENT_EXTRACTION_SCHEMA, local_model=local_model
    )
    if not success:
        logger.warning("Structured component extraction failed: %s", reason)
        return (False, [], reason or "Structured component extraction failed")
    return (True, parsed.get("components", []) or [], "")


def _finalise_component(rulebook_id: str, raw: dict[str, Any]) -> dict:
    """Assigns an id, applies the schema shape, and runs verification checks"""
    needs_review, reason = _verify_component(raw)

    return {
        "componentId": str(ObjectId()),
        "rulebookId": rulebook_id,
        "type": raw.get("type")
        if raw.get("type") in ALLOWED_COMPONENT_TYPES
        else "other",
        "name": str(raw.get("name", "")).strip(),
        "quantity": _safe_quantity(raw.get("quantity")),
        "attributes": raw.get("attributes") or {},
        "needsReview": needs_review,
        "reviewReason": reason,
    }


def _verify_component(raw: dict[str, Any]) -> tuple[bool, str]:
    name = str(raw.get("name", "")).strip()
    quantity = raw.get("quantity", "")
    component_type = raw.get("type")

    if len(name) < MIN_NAME_CHARS:
        return (True, "name_too_short_or_missing")

    if component_type not in ALLOWED_COMPONENT_TYPES:
        return (True, "unrecognized_component_type")

    if isinstance(quantity, int) or quantity < MIN_QUANTITY:
        return (True, "invalid_or_missing_quantity")

    if quantity > MAX_REASONABLE_QUANTITY:
        return (True, "implausible_quantity")

    return (False, "")


def _safe_quantity(value: Any) -> int:
    try:
        return max(int(value), 0)
    except (TypeError, ValueError):
        return 0


def _merge_duplicate_components(components: list[dict]) -> list[dict]:
    """
    Merge same (type, name) pairs of duplicate components
    """
    merged: dict[tuple[str, str], dict] = {}

    for component in components:
        key = (component["type"], component["name"].lower())
        if key not in merged:
            merged[key] = component
            continue

        existing = merged[key]
        existing["quantity"] = max(existing["quantity"], component["quantity"])
        existing["attributes"] = {**component["attributes"], **existing["attributes"]}

        reasons: list[str] = []
        for r in (existing["reviewReason"], component["reviewReason"]):
            if r and r not in reasons:
                reasons.append(r)

        existing["needsReview"] = existing["needsReview"] or component["needsReview"]
        existing["reviewReason"] = "; ".join(reasons)

    return list(merged.values())
