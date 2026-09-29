from fastapi import APIRouter, BackgroundTasks, HTTPException, Request

from app.jobs.setup_wizard_job import run_setup_wizard_job
from app.services import mongo_service

router = APIRouter()

@router.post("/{rulebook_id}/setup-wizard")
def create_setup_wizard_endpoint(
    rulebook_id: str, background_tasks: BackgroundTasks, request: Request
):
    wizard = mongo_service.get_or_create_setup_wizard(rulebook_id)
    if wizard["job"]["status"] == "queued":
        background_tasks.add_task(
            run_setup_wizard_job, wizard["id"], rulebook_id, request.app.state.ml_models
        )
    return wizard


@router.get(
    "/setup-wizard/{wizard_id}",
    responses={404: {"description": "Setup wizard not found"}},
)
def get_setup_wizard_endpoint(wizard_id: str):
    doc = mongo_service.get_setup_wizard(wizard_id)
    if not doc:
        raise HTTPException(status_code=404, detail="Setup wizard not found")
    return doc