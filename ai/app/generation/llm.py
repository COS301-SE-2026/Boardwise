import logging
import time

from fastapi import HTTPException
from huggingface_hub import InferenceClient
from huggingface_hub.errors import HfHubHTTPError

from app.config import settings

logger = logging.getLogger(__name__)

hf_client = InferenceClient(model="Qwen/Qwen2.5-7B-Instruct", token=settings.HF_TOKEN)


def generate_answer(messages: list[dict], ml_models: dict, max_retries: int = 3) -> str:
    """
    Calls the Hugging Face Serverless API to generate an answer using the provided context.
    Implements a backoff strategy to handle 503 (Cold Start) and 429 (Rate Limit) HTTP errors.
    Trips a circuit breaker to a local model if retries are exhaused.
    """
    base_delay = 2.0

    for attempt in range(max_retries):
        try:
            # Low temperature (0.1) is enforced to keep the LLM analytical and reduce hallucinations
            response = hf_client.chat_completion(
                messages=messages, max_tokens=500, temperature=0.1, stream=False
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
                    break  # Break loop to trigger local fallback model

            logger.exception("Unexpected HTTP error from Hugging Face Inference API.")
            break

        except Exception:
            logger.exception("Critical error during LLM text generation.")
            break

    logger.warning("Executing local fallback model...")
    try:
        local_model = ml_models.get("local_llm")
        if not local_model:
            raise ValueError("Local LLM model missing from application state.")

        response = local_model.create_chat_completion(
            messages=messages, max_tokens=500, temperature=0.1
        )

        raw_content = response["choices"][0]["message"]["content"]
        answer = (raw_content or "").strip()
        logger.info("Successfully generated LLM response from local model.")
        return answer
    except Exception:
        logger.exception("FATAL: Local fallback LLM also failed.")
        raise HTTPException(
            status_code=503, detail="The AI service is currently unavailable."
        )
