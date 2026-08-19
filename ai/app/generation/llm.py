import logging
import os
import time

from fastapi import HTTPException
from huggingface_hub import InferenceClient
from huggingface_hub.errors import HfHubHTTPError

logger = logging.getLogger(__name__)

hf_client = InferenceClient(
    model="Qwen/Qwen2.5-7B-Instruct", token=os.getenv("HF_TOKEN")
)


def generate_answer(messages: list[dict], max_retries: int = 3) -> str:
    """
    Calls the Hugging Face Serverless API to generate an answer using the provided conversational context.
    Implements a backoff strategy to handle 503 (Cold Start) and 429 (Rate Limit) HTTP errors.
    """
    base_delay = 2.0

    for attempt in range(max_retries):
        try:
            # Low temperature (0.1) is enforced to keep the LLM analytical and prevent hallucinations
            response = hf_client.chat_completion(
                messages=messages, max_tokens=500, temperature=0.1, stream=False
            )

            raw_content = response.choices[0].message.content
            answer = (raw_content or "").strip()
            logger.info("Successfully generated LLM response from Hugging Face API.")
            return answer

        except HfHubHTTPError as error:
            status_code = error.response.status_code

            if status_code in (503, 429):
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
                        "Hugging Face API exhausted retries for status code %d.",
                        status_code,
                    )
                    raise HTTPException(
                        status_code=503,
                        detail="The AI service is currently warming up or experiencing high traffic. Please try again in a few seconds.",
                    )

            logger.exception("Unexpected HTTP error from Hugging Face Inference API.")
            raise HTTPException(
                status_code=500,
                detail="An internal error occurred while communicating with the AI service.",
            )

        except Exception:
            logger.exception("Critical error during LLM text generation.")
            raise HTTPException(
                status_code=500,
                detail="An unexpected error occured during answer generation.",
            )
    raise HTTPException(status_code=500, detail="Answer generation failed")
