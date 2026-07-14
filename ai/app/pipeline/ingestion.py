import io
import logging
from PIL import Image
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
            mongo_service.update_ingestion_job(job_id, "Sanitise", "Failed", sanitise_reason)
            mongo_service.update_rulebook_status(rulebook_id, "Failed")
            return

        # =========== Stage 2: Extract ===========
        mongo_service.update_ingestion_job(job_id, "Extract", "Processing")

        extract_success, extracted_text = extract_text(file_bytes)

        if not extract_success:
            mongo_service.update_ingestion_job(job_id, "Extract", "Failed", "Extraction failed.")
            mongo_service.update_rulebook_status(rulebook_id, "Failed")
            return

        # =========== Stage 3: Chunk ===========
        mongo_service.update_ingestion_job(job_id, "Chunk", "Processing")

        chunk_success, chunk_list, chunk_reason = generate_chunks(extracted_text)

        if not chunk_success:
            mongo_service.update_ingestion_job(job_id, "Chunk", "Failed", chunk_reason)
            mongo_service.update_rulebook_status(rulebook_id, "Failed")
            return

        # =========== Storage & Finalisation ===========
        # Storage
        pdf_key = r2_service.generate_pdf_key(rulebook_id, filename)

        pdf_upload = r2_service.upload_to_r2(file_bytes, pdf_key, content_type="application/pdf")

        if not pdf_upload:
            mongo_service.update_ingestion_job(job_id, "Store", "Failed", "R2 Upload Failed")
            mongo_service.update_rulebook_status(rulebook_id, "Failed")
            return

        # Finalisation
        mongo_service.update_rulebook_r2_pdf_key(rulebook_id, pdf_key)

        mongo_service.create_rulebook_text(rulebook_id, chunk_list)

        mongo_service.update_rulebook_status(rulebook_id, "Ready", 1)

        mongo_service.update_ingestion_job(job_id, "Store", "Completed")

        logger.info("Pipeline completed successfully for rulebook %s", rulebook_id)
    except Exception:
        logger.exception("Critical pipeline crash for rulebook %s", rulebook_id)

        mongo_service.update_rulebook_status(rulebook_id=rulebook_id, status="Failed")
        mongo_service.update_ingestion_job(
            job_id=job_id,
            stage="Unknown",
            job_status="Failed",
            failure_reason="Critical system crash during pipeline execution."
        )
