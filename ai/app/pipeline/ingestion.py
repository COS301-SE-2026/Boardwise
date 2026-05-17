from app.services.sanitiser import sanitise_pdf
from app.services.extractor import extract_text
from app.services.r2_service import upload_pdf, generate_r2_key
from app.services import mongo_service


def run_ingestion_pipeline(
    file_bytes: bytes,
    filename: str,
    rulebook_id: str,
    job_id: str
):
    # --- Stage 1: Sanitise ---
    mongo_service.update_ingestion_job(job_id, "Sanitise", "Processing")
    is_safe, reason = sanitise_pdf(file_bytes)

    if not is_safe:
        mongo_service.update_ingestion_job(job_id, "Sanitise", "Failed", reason)
        mongo_service.update_rulebook_status(rulebook_id, "Failed")
        return

    # --- Stage 2: Extract ---
    mongo_service.update_ingestion_job(job_id, "Extract", "Processing")
    success, extracted_text = extract_text(file_bytes)

    if not success:
        mongo_service.update_ingestion_job(
            job_id, "Extract", "Failed",
            "Text extraction failed - scanned or image-based PDF"
        )
        mongo_service.update_rulebook_status(rulebook_id, "Failed")
        return

    # --- Storage & Finalisation ---
    r2_key = generate_r2_key(rulebook_id, filename)
    upload_pdf(file_bytes, r2_key)
    mongo_service.update_rulebook_r2_key(rulebook_id, r2_key)

    mongo_service.create_rulebook_text(rulebook_id, extracted_text)
    mongo_service.update_ingestion_job(job_id, "Extract", "Completed")
    mongo_service.update_rulebook_status(rulebook_id, "Ready", version=1)