from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel
from datetime import datetime
from typing import Optional

class BaseAPIModel(BaseModel):
    """
    A base model that automatically converts Python snake_case
    to JSON camelCase for Spring Boot compatibility.
    """
    model_config = ConfigDict(
        alias_generator=to_camel,
        populate_by_name=True,
        from_attributes=True
    )

class UploadResponse(BaseAPIModel):
    """Returned immediately (HTTP 202) when a rulebook is accepted for processing."""
    message: str = "Rulebook upload accepted and ingestion started."
    rulebook_id: str
    job_id: str

class JobStatusResponse(BaseAPIModel):
    """Returned when the frontend polls for pipeline progress"""
    job_id: str = Field(..., alias="id")
    rulebook_id: str
    stage: str
    job_status: str
    failure_reason: Optional[str] = None
    started_at: datetime
    completed_at: Optional[datetime] = None

class RulebookResponse(BaseAPIModel):
    """Returned when viewing a specific rulebook's metadata."""
    rulebook_id: str = Field(..., alias="id")
    game_id: str
    title: str
    edition: str
    status : str
    version: int
    contributor_username: str
    description: str
    language: str
    r2_pdf_key: Optional[str] = None
    r2_cover_key: Optional[str] = None
    uploaded_at: datetime
    updated_at: datetime
