import logging
import threading
from app.services import mongo_service
from app.jobs.setup_wizard_job import run_setup_wizard_job
from app.dependencies import verify_jwt, settings


from concurrent.futures import ThreadPoolExecutor
from typing import Annotated

from bson import ObjectId
from fastapi import APIRouter, Request, Depends, BackgroundTasks, HTTPException

router = APIRouter()

@router.post("/{rulebook_id}/setup-wizard")
def create_setup_wizard_endpoint(rulebook_id: str, background_tasks: BackgroundTasks, request: Request):
    wizard = mongo_service.get_or_create_setup_wizard(rulebook_id)
    if wizard["job"]["status"] == "queued":
        background_tasks.add_task(run_setup_wizard_job, wizard["id"], rulebook_id, request.app.state.ml_models )
    return wizard

@router.get("/setup-wizard/{wizard_id}")
def get_setup_wizard_endpoint(wizard_id: str):
    doc = mongo_service.get_setup_wizard(wizard_id)
    if not doc:
        raise HTTPException(404)
    return doc