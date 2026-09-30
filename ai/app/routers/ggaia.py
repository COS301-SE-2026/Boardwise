import logging
import uuid
import httpx2

from fastapi import (
    APIRouter, 
    BackgroundTasks, 
    Request, 
    status,
    Depends
)
from typing import Annotated

from ai.app.services.ggaia_new_game import generate_new_game, InfeasiblePair
from app.retrieval.ggaia_retrieval import load_inputs
from app.schemas.schemas import GGAIARequest
from app.dependencies import verify_jwt
from app.config import settings

router = APIRouter(tags=["ggaia"])
logger = logging.getLogger(__name__)

def notify_user(payload: dict):
    try:
        request_url = settings.SPRING_API_BASE + ''
        httpx2.post(
            request_url,
            json=payload,
            headers={"X-Internal-Token": settings.INTERNAL_WEBHOOK_SECRET}
        )
    except Exception:
        logger.exception(f"Failed to send \"notify_user\" request to spring backend for job {payload['job_id']}")

def run_generation_job(
    ml_models: dict, 
    request_body: GGAIARequest,
    user_id: str,
    job_id: str
) -> None:
    try:
        inputs = load_inputs(request_body, user_id)
        # will be assigned to a variable for the next function when it is available [this is just for sonarqube]
        generate_new_game(inputs, ml_models) if request_body.type == 'NEW' else None # <- replace with scale method

        # somewhere we need to tie things to the user fr
        
        # do some saving or sumn
        # generate_rulebook(result) <- send to model and make rulebook
        spring_alert = {
            "status": "success"
        }
    except InfeasiblePair as in_pair:
        spring_alert = {
            "status": "failed",
            "reason": f"Selected pair deemed infeasible for generation. Reason: {str(in_pair)}"
        }
    except Exception as exc:
        logger.exception(f"Generation job {job_id} failed")
        spring_alert = {
            "status": "failed",
            "reason": f"Something went wrong during game generation. Reason: {str(exc)}"
        }

    spring_alert["userId"] = user_id
    notify_user(spring_alert)

@router.post("/generate", status_code=status.HTTP_202_ACCEPTED)
async def game_architect(
    request: Request,
    background: BackgroundTasks,
    request_body: GGAIARequest,
    jwt_payload: Annotated[dict, Depends(verify_jwt)]
) -> dict[str, str]:
    
    ml_models = request.app.state.ml_models
    job_id = str(uuid.uuid4())
    background.add_task(run_generation_job, ml_models, request_body, jwt_payload['sub'], job_id)
    
    return {
        "job_id" : job_id,
        "status": "job accepted"
    }
