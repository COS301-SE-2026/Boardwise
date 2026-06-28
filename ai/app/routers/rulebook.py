from fastapi import APIRouter, UploadFile, File, Form, HTTPException, status, BackgroundTasks
from typing import Optional
from app.config import settings
from app.pipeline.ingestion import run_ingestion_pipeline

router = APIRouter(prefix="/api/vault/rulebooks", tags=["rulebooks"])

@router.post("",status_code=status.HTTP_202_ACCEPTED)
async def upload_rulebook(
    background_tasks: BackgroundTasks,
    title: str = Form(...),
    edition: Optional[str] = Form(None),
    language: str = Form(...),
    file: UploadFile = File(...)
):
    # Validate file type
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF files are allowed."
        )

    # Read and validate file size
    file_bytes = await file.read()
    max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024
    if len(file_bytes) > max_bytes:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"File exceeds {settings.MAX_FILE_SIZE_MB}MB limit."
        )

    # Send bytes to background task
    background_tasks.add_task(run_ingestion_pipeline, file_bytes, file.filename)

    return {
        "rulebook_id": "mock_id",
        "title": title,
        "edition": edition,
        "status": "Processing",
        "message":"Rulebook upload accepted. Processing in background."
    }