import io
import logging
from PIL import Image
from app.services import mongo_service, r2_service
from app.services.extractor import extract_text_and_cover
from app.services.sanitiser import sanitise_pdf
from app.services.chunker import generate_chunks

logger = logging.getLogger(__name__)

def run_ingestion_pipeline(
    file_bytes:bytes,
    filename: str,
    rulebook_id: str,
    job_id: str,
    custom_cover_bytes: bytes | None = None,
    custom_cover_mime: str | None = None,
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
            mongo_service.update_ingestion_job(job_id, "Sanitise", sanitise_reason)
            mongo_service.update_rulebook_status(rulebook_id, "Failed")
            return

        # =========== Stage 2: Extract ===========
        mongo_service.update_ingestion_job(job_id, "Extract", "Processing")

        extract_success, extracted_text, auto_cover_bytes = extract_text_and_cover(file_bytes)

        if not extract_success or auto_cover_bytes is None:
            mongo_service.update_ingestion_job(job_id, "Extract", "Extraction failed.")
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
        final_cover_bytes = custom_cover_bytes if custom_cover_bytes else auto_cover_bytes

        if custom_cover_bytes and custom_cover_mime != "image/png":
            img = Image.open(io.BytesIO(final_cover_bytes))
            png_buffer = io.BytesIO()
            img.save(png_buffer, format="PNG")
            final_cover_bytes = png_buffer.getvalue()

        pdf_key = r2_service.generate_pdf_key(rulebook_id, filename)
        cover_key = r2_service.generate_pdf_cover_key(rulebook_id)

        pdf_upload = r2_service.upload_to_r2(file_bytes, pdf_key, content_type="application/pdf")
        cover_upload = r2_service.upload_to_r2(final_cover_bytes, cover_key, content_type="image/png")

        if not pdf_upload or not cover_upload:
            mongo_service.update_ingestion_job(job_id, "Store", "Failed", "R2 Upload Failed")
            mongo_service.update_rulebook_status(rulebook_id, "Failed")
            return

        # Finalisation
        mongo_service.update_rulebook_r2_pdf_key(rulebook_id, pdf_key)
        mongo_service.update_rulebook_r2_cover_key(rulebook_id, cover_key)

        mongo_service.create_rulebook_text(rulebook_id, chunk_list)

        mongo_service.update_rulebook_status(rulebook_id, "Ready", 1)

        mongo_service.update_ingestion_job(job_id, "Store", "Completed")

        logger.info(f"Pipeline completed successfully for rulebook {rulebook_id}")
    except Exception as e:
        logger.error(f"Critical pipeline crash for rulebook {rulebook_id}: {str(e)}", exc_info=True)

        mongo_service.update_rulebook_status(rulebook_id=rulebook_id, status="Failed")
        mongo_service.update_ingestion_job(
            job_id=job_id,
            stage="Unknown",
            job_status="Failed",
            failure_reason="Critical system crash during pipeline execution."
        )