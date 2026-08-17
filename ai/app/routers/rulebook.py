import logging
from typing import Optional
from fastapi import APIRouter, BackgroundTasks, UploadFile, File, Form, Depends,HTTPException, status
from app.dependencies import verify_jwt
from app.models.schemas import UploadResponse
from app.services import mongo_service
from app.config import settings
from app.pipeline.ingestion import run_ingestion_pipeline
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
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF files are allowed."
        )

    max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024
    chunk_size = 1024 * 1024
    file_bytes = bytearray()

    while chunk := await file.read(chunk_size):
        file_bytes.extend(chunk)
        if len(file_bytes) > max_bytes:
            raise HTTPException(
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                detail=f"File exceeds {settings.MAX_FILE_SIZE_MB}MB limit."
            )

    file_bytes = bytes(file_bytes)

    if not file_bytes or not file_bytes.startswith(b"%PDF"):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="The uploaded file is empty or corrupted."
        )

    contributor_id = payload.get("sub")
    if not contributor_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="sub is missing from token."
        )

    if not ObjectId.is_valid(contributor_id):
        logger.warning("Upload rejected: malformed sub claim '%s'.", contributor_id)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid token subject."
        )

    try:
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
        logger.exception("Failed to initialise upload.")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An internal server error occurred while initialising the upload."
        ) from e

    safe_filename = file.filename or "untitled_rulebook.pdf"
    background_tasks.add_task(
        run_ingestion_pipeline,
        file_bytes=file_bytes,
        filename=safe_filename,
        rulebook_id=rulebook_id,
        job_id=job_id
    )

    logger.info("Rulebook upload accepted.")

    return UploadResponse(
        message="Rulebook upload accepted. Ingestion has started.",
        rulebook_id=rulebook_id,
        job_id=job_id
    )
