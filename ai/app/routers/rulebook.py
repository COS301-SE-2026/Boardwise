import logging
from typing import Annotated

from bson import ObjectId
from fastapi import (
    APIRouter,
    BackgroundTasks,
    Depends,
    File,
    Form,
    HTTPException,
    Request,
    UploadFile,
    status,
)

from app.config import settings
from app.dependencies import verify_index_ready, verify_jwt
from app.generation.llm import generate_answer
from app.generation.prompt import build_chat_messages
from app.ingestion.ingestion import run_ingestion_pipeline
from app.models.schemas import Citation, QueryRequest, QueryResponse, UploadResponse
from app.retrieval.retriever import retrieve_context
from app.services import mongo_service
from app.utils.logging_utils import sanitise_log_input

logger = logging.getLogger(__name__)

router = APIRouter(tags=["rulebooks"])

SAFE_TEXT_PATTERN = r"^[\w\s\-.,&'\(\)!?]+$"


@router.post(
    "/upload",
    response_model=UploadResponse,
    status_code=status.HTTP_202_ACCEPTED,
    responses={
        500: {
            "description": "Internal Server Error",
            "content": {
                "application/json": {
                    "example": {
                        "detail": "An internal server error occurred while initialising the upload."
                    }
                }
            },
        }
    },
)
async def upload_rulebook(
    background_tasks: BackgroundTasks,
    request: Request,
    title: Annotated[
        str,
        Form(
            min_length=1,
            max_length=150,
            strip_whitespace=True,
            pattern=SAFE_TEXT_PATTERN,
        ),
    ],
    language: Annotated[
        str,
        Form(
            min_length=2, max_length=10, strip_whitespace=True, pattern=r"^[a-zA-Z\-]+$"
        ),
    ],
    file: Annotated[UploadFile, File()],
    payload: Annotated[dict, Depends(verify_jwt)],
    edition: Annotated[
        str | None,
        Form(max_length=150, strip_whitespace=True, pattern=r"^[\w\s\-.,&'\(\)!?]*$"),
    ] = None,
):
    """
    Accepts a PDF rulebook upload, initialises the database state,
    and starts the ingestion pipeline.
    """
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF files are allowed.",
        )

    max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024
    chunk_size = 1024 * 1024
    file_bytes = bytearray()

    while chunk := await file.read(chunk_size):
        file_bytes.extend(chunk)
        if len(file_bytes) > max_bytes:
            raise HTTPException(
                status_code=status.HTTP_413_CONTENT_TOO_LARGE,
                detail=f"File exceeds {settings.MAX_FILE_SIZE_MB}MB limit.",
            )

    file_bytes = bytes(file_bytes)

    if not file_bytes or not file_bytes.startswith(b"%PDF"):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_CONTENT,
            detail="The uploaded file is empty or corrupted.",
        )

    contributor_id = payload.get("sub")
    if not contributor_id:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="sub is missing from token.",
        )

    if not ObjectId.is_valid(contributor_id):
        logger.warning(
            "Upload rejected: malformed sub claim '%s'.",
            sanitise_log_input(contributor_id),
        )
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token subject."
        )

    try:
        rulebook_id, job_id = mongo_service.create_rulebook_and_job(
            title=title,
            edition=edition,
            contributor_id=contributor_id,
            language=language,
        )

    except ValueError as e:
        logger.warning("Upload rejected: %s", str(e))
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST, detail="Upload rejected"
        ) from e

    except Exception as e:
        logger.exception("Failed to initialise upload.")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="An internal server error occurred while initialising the upload.",
        ) from e

    embedding_model = request.app.state.ml_models["embedding_model"]

    safe_filename = file.filename or "untitled_rulebook.pdf"
    background_tasks.add_task(
        run_ingestion_pipeline,
        file_bytes=file_bytes,
        filename=safe_filename,
        rulebook_id=rulebook_id,
        job_id=job_id,
        embedding_model=embedding_model,
    )

    logger.info("Rulebook upload accepted.")

    return UploadResponse(
        message="Rulebook upload accepted. Ingestion has started.",
        rulebook_id=rulebook_id,
        job_id=job_id,
    )


@router.post(
    "/{rulebook_id}/query",
    response_model=QueryResponse,
    dependencies=[Depends(verify_jwt)],
    responses={
        500: {
            "description": "Internal Server Error",
            "content": {
                "application/json": {
                    "example": {
                        "detail": "An unexpected error occurred while processing your query."
                    }
                }
            },
        }
    },
)
async def query_rulebook(
    rulebook_id: str,
    payload: QueryRequest,
    request: Request,
    _: Annotated[None, Depends(verify_index_ready)],
):
    """
    Executes a RAG query against a specific rulebook.
    Retrieves vector context, scores relevance, and generates a grounded LLM answer.
    """
    try:
        query = payload.query
        ml_models = request.app.state.ml_models

        retrieved_chunks = retrieve_context(query, rulebook_id, ml_models)

        if not retrieved_chunks:
            logger.info(
                "No context found for rulebook %s. Bypassing LLM generation.",
                sanitise_log_input(rulebook_id),
            )
            return QueryResponse(
                answer="I cannot find the answer to this rule in the provided text.",
                citations=[],
            )

        messages = build_chat_messages(query, retrieved_chunks)
        answer = generate_answer(messages)

        citations = [
            Citation(
                chunk_id=str(chunk.get("chunkId", "unknown")),
                index=chunk.get("index", 0),
                content=chunk.get("content", ""),
                relevance_score=chunk.get("relevanceScore", 0.0),
            )
            for chunk in retrieved_chunks
        ]
        logger.info(
            "Successfully processed query for rulebook %s.",
            sanitise_log_input(rulebook_id),
        )
        return QueryResponse(answer=answer, citations=citations)
    except HTTPException:
        raise
    except Exception:
        logger.exception(
            "Unexpected error occurred while querying rulebook %s",
            sanitise_log_input(rulebook_id),
        )
        raise HTTPException(
            status_code=500,
            detail="An unexpected error occurred while processing your query.",
        )
