from fastapi import (
    APIRouter, 
    BackgroundTasks, 
    Request, 
    status,
    Depends
)
from typing import Annotated

from app.services.ggaia_service import generate_new_game, InfeasiblePair
from app.retrieval.ggaia_retrieval import load_inputs
from app.dependencies import verify_jwt

router = APIRouter(tags=["ggaia"])

def run_generation_job(
    ml_models: dict, 
    games_for_inspo: list[str] | str
) -> None:
    try:
        inputs = load_inputs(games_for_inspo)
        result = generate_new_game(inputs, ml_models)
    except InfeasiblePair as in_pair:
        pass


@router.post("/generate", status_code=status.HTTP_202_ACCEPTED)
async def game_architect(
    request: Request,
    background: BackgroundTasks,
    request_body: Annotated[dict, Depends(verify_jwt)]
):
    ml_models = request.app.state.ml_models
    background.add_task(run_generation_job, ml_models)
