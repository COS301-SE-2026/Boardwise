import logging
import time
from datetime import datetime, timezone

from llama_cpp import Llama
from sentence_transformers import SentenceTransformer

from app.ingestion.chunker import filter_out_decorative_chunks, generate_chunks
from app.ingestion.extractor import extract_text
from app.ingestion.game_architect.component_extractor import extract_components
from app.ingestion.sanitiser import sanitise_pdf
from app.ingestion.vectoriser import vectorise_chunks
from app.services import lancedb_service, mongo_service, r2_service

logger = logging.getLogger(__name__)

LANCEDB_WRITE_MAX_RETRIES = 3
LANCEDB_WRITE_BASE_DELAY_SECONDS = 1.0


def _write_chunks_to_lancedb_with_retry(chunks: list[dict]) -> tuple[bool, str]:
    """Returns (success, failure_reason)"""
    for attempt in range(LANCEDB_WRITE_MAX_RETRIES):
        try:
            lancedb_service.write_chunks(chunks)
            return (True, "")
        except Exception as error:
            is_last_attempt = attempt == LANCEDB_WRITE_MAX_RETRIES - 1
            logger.warning(
                "LanceDB write attempt %d/%d failed: %s",
                attempt + 1,
                LANCEDB_WRITE_MAX_RETRIES,
                error,
            )
            if is_last_attempt:
                logger.exception("LanceDB write exhausted retries.")
                return (False, "Failed to write vectors to LanceDB after retries.")
            time.sleep(LANCEDB_WRITE_BASE_DELAY_SECONDS * (2**attempt))
    return (False, "Failed to write vectors to LanceDB.")


def _run_component_extraction(
    rulebook_id: str, job_id: str, chunk_list: list[dict], local_model: Llama | None
) -> bool:
    """Executes the component extraction stage"""
    mongo_service.update_ingestion_job(job_id, "ComponentExtraction", "Processing")
    try:
        game_slug = mongo_service.get_game_slug_for_rulebook(rulebook_id)
    except ValueError as v:
        reason = f"Cannot resolve game for rulebook: {v}"
        logger.warning("Failing pipeline for rulebook %s: %s", rulebook_id, reason)
        mongo_service.mark_pipeline_failed(
            rulebook_id, job_id, "ComponentExtraction", reason
        )
        return False

    component_success, component_list, component_reason = extract_components(
        chunk_list, rulebook_id, game_slug, local_model=local_model
    )

    if not component_success:
        logger.warning(
            "Component extraction failed for rulebook %s: %s",
            rulebook_id,
            component_reason,
        )
    else:
        try:
            mongo_service.store_extracted_components(rulebook_id, component_list)
            logger.info(
                "Stored %d components for rulebook %s",
                len(component_list),
                rulebook_id,
            )
        except Exception:
            logger.exception(
                "Failed to persist components for rulebook %s", rulebook_id
            )
    return True


def _finalise_storage(
    file_bytes: bytes,
    filename: str,
    rulebook_id: str,
    job_id: str,
    extracted_text: str,
    vectorised_chunks: list[dict],
    pending_review: bool,
    quality_summary: dict,
) -> None:
    """Handles file uploads, updating document states, and LanceDB persistence"""
    # Storage
    pdf_key = r2_service.generate_pdf_key(rulebook_id, filename)

    pdf_upload = r2_service.upload_to_r2(
        file_bytes, pdf_key, content_type="application/pdf"
    )
    debug_key = f"rulebooks/{rulebook_id}/raw_extracted.md"
    r2_service.upload_to_r2(
        extracted_text.encode("utf-8"), debug_key, content_type="text/markdown"
    )

    if not pdf_upload:
        mongo_service.mark_pipeline_failed(
            rulebook_id, job_id, "Store", "R2 Upload Failed"
        )
        return

    current_time = datetime.now(timezone.utc)
    for chunk in vectorised_chunks:
        chunk["rulebookId"] = rulebook_id
        chunk["createdAt"] = current_time
        chunk["updatedAt"] = current_time

    # Finalisation
    mongo_service.store_rulebook_text_and_pdf_key(
        rulebook_id, pdf_key, vectorised_chunks
    )

    if pending_review:
        mongo_service.mark_rulebook_pending_review(
            rulebook_id, job_id, quality_summary.get("reason", "")
        )
        logger.info("Pipeline completed (quarantined) for rulebook %s", rulebook_id)
        return

    lancedb_success, lancedb_reason = _write_chunks_to_lancedb_with_retry(
        vectorised_chunks
    )

    if not lancedb_success:
        mongo_service.mark_pipeline_failed(rulebook_id, job_id, "Store", lancedb_reason)
        return

    mongo_service.mark_rulebook_ready(rulebook_id, job_id)

    logger.info("Pipeline completed successfully for rulebook %s", rulebook_id)


def run_ingestion_pipeline(
    file_bytes: bytes,
    filename: str,
    rulebook_id: str,
    job_id: str,
    embedding_model: SentenceTransformer,
    *,
    local_model: Llama | None = None,
):
    """
    Executes the background ingestion pipeline for a rulebook PDF.
    Updates MongoDB state at every stage and handles R2 storage.
    """
    try:
        # =========== Stage 1: Sanitise ===========
        mongo_service.update_ingestion_job(job_id, "Sanitise", "Processing")

        sanitise_success, sanitise_reason = sanitise_pdf(file_bytes)

        if not sanitise_success:
            mongo_service.mark_pipeline_failed(
                rulebook_id, job_id, "Sanitise", sanitise_reason
            )
            return

        # =========== Stage 2: Extract ===========
        mongo_service.update_ingestion_job(job_id, "Extract", "Processing")

        extract_success, extracted_text, extract_reason, blocks_cache = extract_text(
            file_bytes, rulebook_id
        )

        if not extract_success:
            mongo_service.mark_pipeline_failed(
                rulebook_id, job_id, "Extract", extract_reason
            )
            return

        # =========== Stage 3: Chunk ===========
        mongo_service.update_ingestion_job(job_id, "Chunk", "Processing")

        chunk_success, chunk_list, chunk_reason = generate_chunks(blocks_cache)

        if not chunk_success:
            mongo_service.mark_pipeline_failed(
                rulebook_id, job_id, "Chunk", chunk_reason
            )
            return

        before = len(chunk_list)
        chunk_list = filter_out_decorative_chunks(chunk_list)
        logger.info(
            "Filtered chunks: %d -> %d (rulebook %s)",
            before,
            len(chunk_list),
            rulebook_id,
        )

        if not chunk_list:
            mongo_service.mark_pipeline_failed(
                rulebook_id,
                job_id,
                "Chunk",
                "All chunks classified as decorative thus there is nothing to index",
            )
            return

        # ========== Quality Gate ==========

        quality_summary = blocks_cache.get("qualitySummary", {"outcome": "ready"})
        pending_review = quality_summary.get("outcome") == "pending_review"
        pending_reason = quality_summary.get("reason", "")

        if pending_review:
            logger.warning(
                "Rulebook %s flagged for review pre-vectorisation: %s",
                rulebook_id,
                pending_reason,
            )

        # =========== Stage 4: Component Extraction ===========
        if pending_review:
            logger.info(
                "Skipping component extraction for rulebook %s (pending review)",
                rulebook_id,
            )
        else:
            if not _run_component_extraction(
                rulebook_id, job_id, chunk_list, local_model
            ):
                return

        # =========== Stage 5: Vectorise ===========
        if pending_review:
            vectorised_chunks = chunk_list
        else:
            mongo_service.update_ingestion_job(job_id, "Vectorise", "Processing")
            vector_success, vectorised_chunks, vector_reason = vectorise_chunks(
                chunk_list, embedding_model
            )

            if not vector_success:
                mongo_service.mark_pipeline_failed(
                    rulebook_id, job_id, "Vectorise", vector_reason
                )
                return

        # =========== Stage 6: Storage & Finalisation ===========
        _finalise_storage(
            file_bytes,
            filename,
            rulebook_id,
            job_id,
            extracted_text,
            vectorised_chunks,
            pending_review,
            quality_summary,
        )
    except Exception:
        logger.exception("Critical pipeline crash for rulebook %s", rulebook_id)
        mongo_service.mark_pipeline_failed(
            rulebook_id,
            job_id,
            "Unknown",
            "Critical system crash during pipeline execution.",
        )
