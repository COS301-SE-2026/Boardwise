import json
import logging
import random
import threading
import time
from typing import Any

import httpx
from google import genai
from google.genai import types
from google.genai.errors import APIError
from llama_cpp import Llama, LlamaGrammar
from zhipuai import ZhipuAI

from app.config import settings
from app.ingestion.enums.lm_enums import LmStatus
from app.ingestion.utils.api_retry_utils import (
    handle_gemini_api_error,
    is_transient_glm_error,
)

logger = logging.getLogger(__name__)

GEMINI_TEXT_MODEL = "gemini-3.5-flash-lite"
GLM_TEXT_MODEL = "glm-4.6v-flash"

TEXT_REQUEST_TIMEOUT_SECONDS = 60
TEXT_MAX_OUTPUT_TOKENS = 2048
TEXT_BASE_RETRY_DELAY_SECONDS = 2.0
TEXT_TEMPERATURE = 0.0  # Deterministic output for consistent extractions (no need for model creativity)

LOCAL_MAX_PROMPT_CHARS = 3000
LOCAL_MAX_COMPLETION_TOKENS = 1024


TRANSIENT_STATUS_CODES = {429, 500, 502, 503, 504}


DEFAULT_SYSTEM_PROMPT = (
    "You are a precise data-extraction assistant. "
    "Extract data only from the given text. "
    "Return JSON only, exactly matching the schema. "
    "Never add facts not stated in the text."
)


_client: genai.Client | None = None
_glm_client = (
    ZhipuAI(api_key=settings.GLM_API_KEY)
    if getattr(settings, "GLM_API_KEY", None)
    else None
)

_local_llm_lock = threading.Lock()

# Grammar compilation has a fixed cost per schema. We can cache by schema string
# such that there is no need to recompile it for repeated calls for the same schema
_grammar_cache: dict[str, LlamaGrammar] = {}
_grammar_lock = threading.Lock()


def _get_gemini_client() -> genai.Client | None:
    global _client
    if _client is not None:
        return _client
    if not getattr(settings, "GEMINI_API_KEY", None):
        return None
    http_client = httpx.Client(
        timeout=httpx.Timeout(
            connect=15.0,
            read=TEXT_REQUEST_TIMEOUT_SECONDS,
            write=30.0,
            pool=15.0,
        ),
    )

    _client = genai.Client(
        api_key=settings.GEMINI_API_KEY,
        http_options=types.HttpOptions(httpx_client=http_client),
    )
    return _client


# ========== Tier 1: Gemini Structured Output ==========


def _extract_json_from_gemini_response(response) -> tuple[str | None, LmStatus]:
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
        logger.warning("Gemini hit MAX_TOKENS during structured extraction.")
        return (combined or None, LmStatus.MAX_TOKENS)

    if not combined:
        return (None, LmStatus.EMPTY)

    return (combined, LmStatus.OK)


def _call_gemini_structured(
    prompt: str, schema: dict, system_prompt: str, max_retries: int = 3
) -> tuple[str | None, LmStatus]:
    client = _get_gemini_client()
    if client is None:
        return (None, LmStatus.NOT_CONFIGURED)

    for attempt in range(max_retries):
        try:
            response = client.models.generate_content(
                model=GEMINI_TEXT_MODEL,
                contents=[system_prompt, prompt],
                config=types.GenerateContentConfig(
                    temperature=TEXT_TEMPERATURE,
                    max_output_tokens=TEXT_MAX_OUTPUT_TOKENS,
                    tools=[],
                    response_mime_type="application/json",
                    response_schema=schema,
                    automatic_function_calling=types.AutomaticFunctionCallingConfig(
                        disable=True
                    ),
                ),
            )
            return _extract_json_from_gemini_response(response)

        except APIError as e:
            should_retry, status = handle_gemini_api_error(
                e,
                attempt,
                max_retries,
                label="LLM",
                base_retry_delay=TEXT_BASE_RETRY_DELAY_SECONDS,
            )
            if should_retry:
                continue
            return (None, status)

        except Exception:
            logger.exception("Unexpected error during Gemini structured call.")
            return (None, LmStatus.API_ERROR)

    return (None, LmStatus.RETRIES_EXHAUSTED)


# ========== Tier 2: GLM Structured Output ==========

_STRUCTURED_OUTPUT_TOOL_NAME = "emit_structured_output"


def _build_glm_tool(schema: dict) -> dict:
    return {
        "type": "function",
        "function": {
            "name": _STRUCTURED_OUTPUT_TOOL_NAME,
            "description": "Emits the extracted data matching the required schema.",
            "parameters": schema,
        },
    }


def _extract_json_from_glm_response(response) -> tuple[str | None, LmStatus]:
    if not response.choices:
        return (None, LmStatus.EMPTY)

    choice = response.choices[0]
    finish_reason = (choice.finish_reason or "").lower()
    message = choice.message

    if finish_reason == "content_filter":
        return (None, LmStatus.SAFETY)

    tool_calls = getattr(message, "tool_calls", None) if message else None
    if tool_calls:
        call = next(
            (c for c in tool_calls if c.function.name == _STRUCTURED_OUTPUT_TOOL_NAME),
            tool_calls[0],
        )
        arguments = (call.function.arguments or "").strip()

        if finish_reason == "length":
            logger.warning("GLM hit max_token limit mid tool-call arguments")
            return (arguments or None, LmStatus.MAX_TOKENS)

        if not arguments:
            return (None, LmStatus.EMPTY)
        return (arguments, LmStatus.OK)

    if finish_reason == "length":
        logger.warning("GLM hit max_tokens before producing a tool call")
        return (None, LmStatus.MAX_TOKENS)
    logger.warning("GLM did not invoke the structured-output tool")
    return (None, LmStatus.EMPTY)


def _call_glm_structured(
    prompt: str, schema: dict, system_prompt: str, max_retries: int = 3
) -> tuple[str | None, LmStatus]:
    if _glm_client is None:
        return (None, LmStatus.NOT_CONFIGURED)

    tool = _build_glm_tool(schema)

    for attempt in range(max_retries):
        try:
            response = _glm_client.chat.completions.create(
                model=GLM_TEXT_MODEL,
                messages=[
                    {"role": "system", "content": system_prompt},
                    {"role": "user", "content": prompt},
                ],
                temperature=TEXT_TEMPERATURE,
                max_tokens=TEXT_MAX_OUTPUT_TOKENS,
                tools=[tool],
                tool_choice={
                    "type": "function",
                    "function": {"name": _STRUCTURED_OUTPUT_TOOL_NAME},
                },  # type: ignore[arg-type]
                stream=False,
                timeout=TEXT_REQUEST_TIMEOUT_SECONDS,
            )

            text, status = _extract_json_from_glm_response(response)
            if status in (
                LmStatus.OK,
                LmStatus.SAFETY,
                LmStatus.EMPTY,
                LmStatus.MAX_TOKENS,
            ):
                return (text, status)

            if attempt < max_retries - 1:
                delay = (TEXT_BASE_RETRY_DELAY_SECONDS * (2**attempt)) + random.uniform(
                    0, 0.5
                )
                time.sleep(delay)
                continue
            return (None, status)
        except Exception as e:
            if is_transient_glm_error(e) and attempt < max_retries - 1:
                delay = (TEXT_BASE_RETRY_DELAY_SECONDS * (2**attempt)) + random.uniform(
                    0, 0.5
                )
                time.sleep(delay)
                continue
            logger.error("GLM structured call failed (attempt %d)", attempt + 1)
            return (None, LmStatus.API_ERROR)
    return (None, LmStatus.RETRIES_EXHAUSTED)


# ========== Tier 3: Local Qwen Structured Output ==========


def _get_grammar(schema: dict) -> LlamaGrammar:
    cache_key = json.dumps(schema, sort_keys=True)

    cached = _grammar_cache.get(cache_key)
    if cached is not None:
        return cached

    with _grammar_lock:
        # Re-check to see if another thread may have compiled it
        cached = _grammar_cache.get(cache_key)
        if cached is not None:
            return cached
        grammar = LlamaGrammar.from_json_schema(cache_key)
        _grammar_cache[cache_key] = grammar
        return grammar


def _call_local_structured(
    prompt: str, schema: dict, system_prompt: str, local_model: Llama
) -> tuple[str | None, LmStatus]:
    grammar = _get_grammar(schema)
    truncated_prompt = prompt[:LOCAL_MAX_PROMPT_CHARS]

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": truncated_prompt},
    ]
    try:
        with _local_llm_lock:
            response = local_model.create_chat_completion(
                messages=messages,  # type: ignore[arg-type]
                grammar=grammar,
                temperature=TEXT_TEMPERATURE,
                max_tokens=LOCAL_MAX_COMPLETION_TOKENS,
            )
    except Exception:
        logger.exception("Local LLM structured call failed.")
        return (None, LmStatus.API_ERROR)

    finish_reason = response["choices"][0].get("finish_reason")  # type: ignore[arg-type]
    text = response["choices"][0]["message"]["content"]  # type: ignore[arg-type]

    if finish_reason == "length":
        # Output was cut off before producing valid JSON
        logger.warning(
            "Local LLM hit max_tokens during component extraction. Output is probably truncated"
        )
        return (None, LmStatus.MAX_TOKENS)
    return (text, LmStatus.OK)


def call_structured_llm(
    prompt: str,
    schema: dict,
    local_model: Llama | None = None,
    system_prompt: str = DEFAULT_SYSTEM_PROMPT,
    max_retries: int = 3,
) -> tuple[bool, dict[str, Any], str]:
    """
    Cascades Gemini (structured output) -> GLM (JSON mode) -> local Qwen (grammar-constrained)
    Returns: (success, parsed_json, failure_reason)
    """
    text, status = _call_gemini_structured(prompt, schema, system_prompt, max_retries)
    if status is LmStatus.OK and text:
        parsed = _safe_json_parse(text)
        if parsed is not None:
            return (True, parsed, "")

    logger.info("Falling back to GLM for structured output (gemini status=%s).", status)

    text, status = _call_glm_structured(prompt, schema, system_prompt, max_retries)
    if status is LmStatus.OK and text:
        parsed = _safe_json_parse(text)
        if parsed is not None:
            return (True, parsed, "")

    if local_model is None:
        logger.warning("No local model available")
        return (
            False,
            {},
            "All cloud LLM providers failed and local fallback is missing",
        )

    logger.info(
        "Falling back to local model for structured output (glm status=%s).", status
    )

    text, status = _call_local_structured(prompt, schema, system_prompt, local_model)
    if status is LmStatus.OK and text:
        parsed = _safe_json_parse(text)
        if parsed is not None:
            return (True, parsed, "")

    return (
        False,
        {},
        f"All structured LLM tiers failed (final status ={status.value})",
    )


def _safe_json_parse(text: str) -> dict[str, Any] | None:
    try:
        return json.loads(text)
    except json.JSONDecodeError:
        logger.error(
            "Structured LLM output failed JSON parsing despite schema constraint"
        )
        return None
