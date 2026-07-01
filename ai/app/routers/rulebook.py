import logging
from fastapi import APIRouter, BackgroundTasks, UploadFile, File, Form, Depends,HTTPException, status
from app.dependencies import verify_jwt
from app.models.schemas import UploadResponse, JobStatusResponse
from app.services import mongo_service
from app.config import settings
from app.pipeline.ingestion import run_ingestion_pipeline
from typing import Optional
from bson import ObjectId

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/api/vault/rulebooks",
    tags=["rulebooks"]
)

@router.post(
    "/upload",
    response_model=UploadResponse,
    status_code=status.HTTP_202_ACCEPTED
)
async def upload_rulebook(
    background_tasks: BackgroundTasks,
    title: str = Form(...),
    edition: Optional[str] = Form(None),
    language: str = Form(...),
    file: UploadFile = File(...),
    payload: dict = Depends(verify_jwt)
):
    """
    Accepts a PDF rulebook upload, initialises the database state,
    and starts the ingestion pipeline.
    """
    # Validate file type
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF files are allowed."
        )

    # Read file and validate file size
    file_bytes = await file.read()
    max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024
    if len(file_bytes) > max_bytes:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"File exceeds {settings.MAX_FILE_SIZE_MB}MB limit."
        )

    if not file_bytes or not file_bytes.startswith(b"%PDF"):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="The uploaded file is empty or corrupted."
        )

    contributor_id = payload.get("userId")
    if not contributor_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="userId is missing from token."
        )

    try:
        # Initialise database
        rulebook_id = mongo_service.create_rulebook(
            title=title,
            edition=edition,
            contributor_id=contributor_id,
            language=language,
            r2_pdf_key=""
        )

        job_id = mongo_service.create_ingestion_job(rulebook_id)

    except ValueError as e:
        logger.warning("Upload rejected: %s", str(e))
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Upload rejected"
        ) from e

    except Exception as e:
        logger.error("Failed to initialise upload: %s", str(e), exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An internal server error occured while initialising the upload."
        ) from e

    # Send bytes to background task
    background_tasks.add_task(
        run_ingestion_pipeline,
        file_bytes=file_bytes,
        filename=file.filename,
        rulebook_id=rulebook_id,
        job_id=job_id
    )

    logger.info("Accepted upload for '%s'. Rulebook ID: %s, Job ID: %s", title, rulebook_id, job_id)

    return UploadResponse(
        message="Rulebook upload accepted. Ingestion has started.",
        rulebook_id=rulebook_id,
        job_id=job_id
    )

@router.get(
    "/status/{job_id}",
    response_model=JobStatusResponse,
    status_code=status.HTTP_200_OK
)
async def get_job_status(job_id: str, payload: dict = Depends(verify_jwt)):
    """
    Allows the frontend to poll for the current status of an ingestion job.
    """
    # Validation
    if not ObjectId.is_valid(job_id):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid job_id format."
        )

    # Database Fetch
    ingestion_job = mongo_service.get_ingestion_job(job_id)

    # Response Handling
    if not ingestion_job:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Ingestion job with id '{job_id}' does not exist."
        )

    return ingestion_job
