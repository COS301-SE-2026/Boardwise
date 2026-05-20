from fastapi import APIRouter, File, UploadFile, Form, Depends, HTTPException, status, BackgroundTasks

from app.dependencies import verify_jwt
from app.models.schemas import RulebookCreatedResponse, ErrorResponse
from app.services import mongo_service
from app.pipeline.ingestion import run_ingestion_pipeline
from app.config import settings

router = APIRouter(prefix="/api/vault/rulebooks", tags=["vault"])

MAX_FILE_SIZE_BYTES = settings.MAX_FILE_SIZE_MB * 1024 * 1024


@router.post(
    "",
    response_model=RulebookCreatedResponse,
    status_code=202,
    responses={
        401: {"model": ErrorResponse},
        413: {"model": ErrorResponse},
        415: {"model": ErrorResponse},
        422: {"model": ErrorResponse}
    }
)
async def upload_rulebook(
    background_tasks: BackgroundTasks,
    file: UploadFile = File(...),
    game_name: str = Form(...),
    edition: str = Form(None),
    game_id: str = Form(...),
    payload: dict = Depends(verify_jwt)
):
    # validate file type
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF files are accepted"
        )

    # read and validate file size
    file_bytes = await file.read()
    if len(file_bytes) > MAX_FILE_SIZE_BYTES:
        raise HTTPException(
            status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            detail=f"File exceeds maximum size of {settings.MAX_FILE_SIZE_MB}MB"
        )

    contributor_id = payload.get("userId")
    if not contributor_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="userId missing from token"
        )

    # generate a placeholder r2 key using the rulebook ID
    rulebook_id = mongo_service.create_rulebook_document(
        game_name=game_name,
        edition=edition,
        contributor_id=contributor_id,
        game_id=game_id,                   
        r2_pdf_key=f"rulebooks/pending/{file.filename}"
    )

    job_id = mongo_service.create_ingestion_job(rulebook_id)

    # run pipeline as background task - returns 202 immediately
    background_tasks.add_task(
        run_ingestion_pipeline,
        file_bytes,
        file.filename,
        rulebook_id,
        job_id
    )

    return RulebookCreatedResponse(
        rulebook_id=rulebook_id,
        game_name=game_name,
        edition=edition,
        game_id=game_id,
        status="Processing",
        message="Rulebook upload accepted. Processing in background."
    )