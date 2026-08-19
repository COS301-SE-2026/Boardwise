from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field
from pydantic.alias_generators import to_camel


class BaseAPIModel(BaseModel):
    model_config = ConfigDict(
        alias_generator=to_camel, populate_by_name=True, from_attributes=True
    )


class UploadResponse(BaseAPIModel):
    message: str = "Rulebook upload accepted and ingestion started."
    rulebook_id: str
    job_id: str


class JobStatusResponse(BaseAPIModel):
    job_id: str = Field(..., alias="id")
    rulebook_id: str
    stage: str
    job_status: str
    failure_reason: str | None = None
    started_at: datetime
    completed_at: datetime | None = None


class QueryRequest(BaseModel):
    query: str = Field(
        ...,
        min_length=3,
        max_length=500,
        description="The user's question about the rulebook.",
    )


class Citation(BaseModel):
    chunkId: str
    index: int
    content: str
    relevanceScore: float


class QueryResponse(BaseModel):
    answer: str
    citations: list[Citation]
