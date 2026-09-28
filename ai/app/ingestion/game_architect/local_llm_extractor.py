import json
import logging
import threading
from typing import Any

from llama_cpp import Llama, LlamaGrammar

from app.ingestion.game_architect.shared_constants import _COMPONENT_EXTRACTION_SCHEMA

logger = logging.getLogger(__name__)

LOCAL_MAX_CANDIDATE_CHARS = 3000
LOCAL_MAX_COMPLETION_TOKENS = 1024

LOCAL_TEMPERATURE = 0.0  # Deterministic output for consistent extractions (no need for model creativity)

# Llama object is not safe for concurrent calls (multiple ingestion jobs) so we need a single process-wide lock to ensure sequential access
_local_llm_lock = threading.Lock()

_SYSTEM_PROMPT = (
    "You are a precise data-extraction assistant. "
    "Extract physical board game components from rulebook text. "
    "Output only JSON matching the schema. "
    "Do not add unstated components."
)

_USER_PROMPT_TEMPLATE = """\
List all distinct physical components and quantities from the rulebook excerpt below.
Exclude rules text; output only the component list.

Excerpt:
---
{text}
---
"""

_COMPONENT_GRAMMAR = LlamaGrammar.from_json_schema(
    json.dumps(_COMPONENT_EXTRACTION_SCHEMA)
)


def extract_components_local(
    candidate_text: str, model: Llama
) -> tuple[bool, list[dict[str, Any]], str]:
    """
    Runs component extraction against the local Qwen2.5-3B gguf model.
    Returns: (success, raw_component_list, failure_reason)
    """
    truncated_text = candidate_text[:LOCAL_MAX_CANDIDATE_CHARS]

    messages = [
        {"role": "system", "content": _SYSTEM_PROMPT},
        {"role": "user", "content": _USER_PROMPT_TEMPLATE.format(text=truncated_text)},
    ]
    try:
        with _local_llm_lock:
            response = model.create_chat_completion(
                messages=messages,
                grammar=_COMPONENT_GRAMMAR,
                temperature=LOCAL_TEMPERATURE,
                max_tokens=LOCAL_MAX_COMPLETION_TOKENS,
            )
    except Exception:
        logger.exception("Local LLM component extraction call failed.")
        return (False, [], "Local model inference failed.")

    finish_reason = response["choices"][0].get("finish_reason")
    raw_content = response["choices"][0]["message"]["content"]

    if finish_reason == "length":
        # Output was cut off before producing valid JSON
        logger.warning(
            "Local LLM hit max_tokens during component extraction. Output is probably truncated"
        )
        return (False, [], "Local model output truncated before completion.")

    try:
        parsed = json.loads(raw_content if isinstance(raw_content, str) else "")
    except json.JSONDecodeError:
        logger.exception("Local LLM returned non-JSON output")
        return (False, [], "Local model output failed JSON parsing.")

    return (True, parsed.get("components", []), "")
