from pydantic import BaseModel
from typing import Optional
from datetime import datetime

class RulebookCreatedResponse(BaseModel):
    rulebook_id: str
    game_name: str
    edition: Optional[str]
    status: str
    message: str

class IngestionStatusResponse(BaseModel):
    rulebook_id: str
    stage: str
    job_status: str
    failure_reason: Optional[str]
    started_at: Optional[datetime]
    completed_at: Optional[datetime]

class ErrorResponse(BaseModel):
    error: str