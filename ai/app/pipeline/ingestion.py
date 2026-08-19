import logging
from datetime import datetime, timezone

from sentence_transformers import SentenceTransformer

from ai.app.ingestion.chunker import generate_chunks
from ai.app.ingestion.extractor import extract_text
from ai.app.ingestion.sanitiser import sanitise_pdf
from ai.app.ingestion.vectoriser import vectorise_chunks
from app.services import mongo_service, r2_service

logger = logging.getLogger(__name__)


def run_ingestion_pipeline(
    file_bytes: bytes,
    filename: str,
    rulebook_id: str,
    job_id: str,
    embedding_model: SentenceTransformer,
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

        extract_success, extracted_text, extract_reason = extract_text(file_bytes)

        if not extract_success:
            mongo_service.mark_pipeline_failed(
                rulebook_id, job_id, "Extract", extract_reason
            )
            return

        # =========== Stage 3: Chunk ===========
        mongo_service.update_ingestion_job(job_id, "Chunk", "Processing")

        chunk_success, chunk_list, chunk_reason = generate_chunks(extracted_text)

        if not chunk_success:
            mongo_service.mark_pipeline_failed(
                rulebook_id, job_id, "Chunk", chunk_reason
            )
            return

        # =========== Stage 4: Vectorise ===========
        mongo_service.update_ingestion_job(job_id, "Vectorise", "Processing")

        vector_success, vectorised_chunks, vector_reason = vectorise_chunks(
            chunk_list, embedding_model
        )

        if not vector_success:
            mongo_service.mark_pipeline_failed(
                rulebook_id, job_id, "Vectorise", vector_reason
            )
            return

        # =========== Stage5: Storage & Finalisation ===========
        # Storage
        pdf_key = r2_service.generate_pdf_key(rulebook_id, filename)

        pdf_upload = r2_service.upload_to_r2(
            file_bytes, pdf_key, content_type="application/pdf"
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
        mongo_service.finalise_rulebook_ingestion(
            rulebook_id, job_id, pdf_key, vectorised_chunks
        )

        logger.info("Pipeline completed successfully for rulebook %s", rulebook_id)
    except Exception:
        logger.exception("Critical pipeline crash for rulebook %s", rulebook_id)
        mongo_service.mark_pipeline_failed(
            rulebook_id,
            job_id,
            "Unknown",
            "Critical system crash during pipeline execution.",
        )
