import logging
from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias.generators import to_camel
from typing import Literal

from ai.app.generation.setup_wizard.setup_wizard_prompt import PHASE_ORDER
from app.generation.setup_wizard.setup_wizard_prompt import build_phase_messages
logger = logging.getLogger(__name__)

class BaseAPIModel(BaseModel):
    model_config = ConfigDict(alias_generator=to_camel, populate_by_name=True, from_attributes=True)

Scope = Literal["shared", "per_player"]

class LLMStep(BaseAPIModel):
    title: str = Field(..., max_length =100)
    instruction: str = Field(..., max_length=400)
    components: list[LLMComponentUse] = []
    scope: Scope 
    optional: bool = False
    source_chunks: list[int] = Field(..., min_length=1)

class LLMPhaseResult(BaseAPIModel):
    steps: list[LLMStep] = []

def generate_phase(phase: str, chunks: list[dict], ml_models: dict)->list[LLMStep]:
    """
    One grammar-constrained call for a single phase. Returns [] on failure
    rather than raising to ensure that a failed phase does not kill the wizard.
    """

    model =ml_models["local_llm"]
    schema = LLMPhaseResult.model_json_schema(by_alias=True)
    messages = build_phase_messages(phase, chunks)
    response  = model.create_chat_completion(
        messages=messages,
        response_format={"type": "json_object", "schema": schema},
        temperature = 0.1,
        repeat_penalty=1.3,
        max_tokens=1250
    )

    raw = response["choices"][0]["message"]["content"]
    try:
        return LLMPhaseResult.model_validate_json(raw).steps
    except Exception:
        logger.warning("Phase '%s' generation failed validation, treating as empty", phase, exc_info=True)