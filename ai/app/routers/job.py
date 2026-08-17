import logging
from fastapi import APIRouter, Depends, HTTPException, Path, status
from app.dependencies import verify_jwt
from app.models.schemas import JobStatusResponse
from app.services import mongo_service
from bson import ObjectId

logger = logging.getLogger(__name__)

router = APIRouter(
    prefix="/api/vault/jobs",
    tags=["jobs"]
)

@router.get(
    "/{jobId}",
    response_model=JobStatusResponse,
    status_code=status.HTTP_200_OK
)
async def get_job_status(job_id: str = Path(..., alias="jobId"), payload: dict = Depends(verify_jwt)):
    """
    Allows the frontend to poll for the current status of an ingestion job.
    """
    if not ObjectId.is_valid(job_id):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid job_id format."
        )

    ingestion_job = mongo_service.get_ingestion_job(job_id)

    if not ingestion_job:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Ingestion job with id '{job_id}' does not exist."
        )

    return ingestion_job
