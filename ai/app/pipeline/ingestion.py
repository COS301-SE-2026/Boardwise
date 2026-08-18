import logging
from app.services import mongo_service, r2_service
from app.services.extractor import extract_text
from app.services.sanitiser import sanitise_pdf
from app.services.chunker import generate_chunks

logger = logging.getLogger(__name__)

def run_ingestion_pipeline(
    file_bytes:bytes,
    filename: str,
    rulebook_id: str,
    job_id: str
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
            mongo_service.mark_pipeline_failed(rulebook_id, job_id, "Sanitise", sanitise_reason)
            return

        # =========== Stage 2: Extract ===========
        mongo_service.update_ingestion_job(job_id, "Extract", "Processing")

        extract_success, extracted_text, extract_reason = extract_text(file_bytes)

        if not extract_success:
            mongo_service.mark_pipeline_failed(rulebook_id, job_id, "Extract", extract_reason)
            return

        # =========== Stage 3: Chunk ===========
        mongo_service.update_ingestion_job(job_id, "Chunk", "Processing")

        chunk_success, chunk_list, chunk_reason = generate_chunks(extracted_text)

        if not chunk_success:
            mongo_service.mark_pipeline_failed(rulebook_id, job_id, "Chunk", chunk_reason)
            return

        # =========== Storage & Finalisation ===========
        # Storage
        pdf_key = r2_service.generate_pdf_key(rulebook_id, filename)

        pdf_upload = r2_service.upload_to_r2(file_bytes, pdf_key, content_type="application/pdf")

        if not pdf_upload:
            mongo_service.mark_pipeline_failed(rulebook_id, job_id, "Store", "R2 Upload Failed")
            return

        # Finalisation
        mongo_service.finalise_rulebook_ingestion(rulebook_id, job_id, pdf_key, chunk_list)

        logger.info("Pipeline completed successfully for rulebook %s", rulebook_id)
    except Exception:
        logger.exception("Critical pipeline crash for rulebook %s", rulebook_id)
        mongo_service.mark_pipeline_failed(rulebook_id, job_id, "Unknown", "Critical system crash during pipeline execution.")
