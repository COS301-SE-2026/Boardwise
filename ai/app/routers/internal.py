import logging
from typing import Annotated

from fastapi import APIRouter, BackgroundTasks, Depends, Request, status

from app.dependencies import verify_internal_token
from app.ingestion.vectoriser import background_vectorise_and_update
from app.schemas import ReEmbedRequest

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Internal Tasks"])


@router.post("/chunks/re-embed", status_code=status.HTTP_202_ACCEPTED)
async def trigger_re_embed(
    payload: ReEmbedRequest,
    background_tasks: BackgroundTasks,
    request: Request,
    token: Annotated[str, Depends(verify_internal_token)],
):
    """
    Webhook triggered by Spring Boot when a chunk is inserted, deleted, updated, redone or undone.
    """
    embedding_model = request.app.state.ml_models["embedding_model"]
    background_tasks.add_task(
        background_vectorise_and_update,
        chunk_id=payload.chunk_id,
        content=payload.content,
        embedding_model=embedding_model,
    )

    return {"status": "accepted", "message": "Re-embedding queued successfully."}
