import logging
import time
import threading

from dataclasses import dataclass
from typing import Literal
from fastapi import HTTPException
from huggingface_hub import InferenceClient
from huggingface_hub.errors import HfHubHTTPError

from app.config import settings

logger = logging.getLogger(__name__)

local_model_lock = threading.Lock()

hf_client = InferenceClient(model="meta-llama/Meta-Llama-3.1-8B-Instruct", token=settings.HF_TOKEN)

Backend = Literal[
    "auto",
    "remote",
    "local"
]

class ModelUnavailableError(RuntimeError):
    pass

@dataclass(frozen=True)
class ModelResult:
    text: str
    source: Literal["remote", "local"]

def _call_remote_llm(
    messages: list[dict], 
    max_retries: int,
    max_tokens: int = 500,
    temperature: float = 0.1
) -> str | None:
    """
    Attempts to fetch a response from the Hugging Face Serverless API with exponential backoff
    """
    base_delay = 2.0

    for attempt in range(max_retries):
        try:
            # Low temperature (0.1) is enforced to keep the LLM analytical and reduce hallucinations
            response = hf_client.chat_completion(
                messages=messages, max_tokens=max_tokens, temperature=temperature, stream=False
            )

            raw_content = response.choices[0].message.content
            answer = (raw_content or "").strip()
            logger.info("Successfully generated LLM response from Hugging Face API.")
            return answer

        except HfHubHTTPError as error:
            status_code = getattr(error.response, "status_code", None)

            if status_code and status_code in (503, 429):
                logger.warning(
                    "Hugging Face API returned %d. Attempt %d of %d.",
                    status_code,
                    attempt + 1,
                    max_retries,
                )
                if attempt < max_retries - 1:
                    time.sleep(base_delay * (2**attempt))
                    continue
                else:
                    logger.error(
                        "Hugging Face API exhausted retries for status code %d. Switching to local model.",
                        status_code,
                    )
                    return None  # To trigger local fallback model

            logger.exception("Unexpected HTTP error from Hugging Face Inference API.")
            return None

        except Exception:
            logger.exception("Critical error during LLM text generation.")
            return None
    return None


def _call_local_fallback(
    messages: list[dict], 
    ml_models: dict,
    max_tokens: int = 500,
    temperature: float = 0.1,
    response_schema: dict | None = None
) -> str:
    """
    Executes the local LLM when the remote one is unavailable.
    """
    logger.info("Executing local model")
    try:
        local_model = ml_models.get("local_llm")
        if not local_model:
            raise ValueError("Local LLM model missing from application state.")

        keyword_args = {}
        if response_schema is not None:
            keyword_args['response_format'] = {
                "type": "json_object",
                "schema": response_schema
            }

        with local_model_lock:
            response = local_model.create_chat_completion(
                messages=messages, max_tokens=max_tokens, temperature=temperature, **keyword_args
            )

        raw_content = response["choices"][0]["message"]["content"]
        answer = (raw_content or "").strip()
        logger.info("Successfully generated LLM response from local model.")
        return answer
    except Exception as exception:
        logger.exception("FATAL: Local LLM failed.")
        raise ModelUnavailableError("Local LLM failed.") from exception
        

def generate_text(
    messages: list[dict],
    ml_models: dict,
    *,
    backend: Backend = "auto",
    max_tokens: int = 500,
    temperature: float = 0.1,
    max_retries: int = 3,
    response_schema: dict | None = None
) -> ModelResult:
    if backend in ("auto", "remote"):
        remote_text = _call_remote_llm(messages, max_retries, max_tokens, temperature)
        if remote_text is not None:
            return ModelResult(remote_text, "remote")
        if backend == "remote":
            raise ModelUnavailableError("Remote LLM unavailable.")
        
    local_text = _call_local_fallback(messages, ml_models, max_tokens, temperature, response_schema)
    return ModelResult(local_text, "local")


def generate_answer(messages: list[dict], ml_models: dict, max_retries: int = 3) -> str:
    """
    Calls the Hugging Face Serverless API to generate an answer using the provided context.
    Implements a backoff strategy to handle 503 (Cold Start) and 429 (Rate Limit) HTTP errors.
    Trips a circuit breaker to a local model if retries are exhausted.
    """
    try:
        return generate_text(messages, ml_models, max_retries=max_retries).text
    except ModelUnavailableError:
        raise HTTPException(
            status_code=503, detail="The AI service is currently unavailable."
        )
