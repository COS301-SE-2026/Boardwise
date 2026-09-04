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


class QueryRequest(BaseAPIModel):
    query: str = Field(
        ...,
        min_length=3,
        max_length=500,
        description="The user's question about the rulebook.",
    )


class Citation(BaseAPIModel):
    chunk_id: str
    index: int
    content: str


class QueryResponse(BaseAPIModel):
    answer: str
    citations: list[Citation]

class ReEmbedRequest(BaseAPIModel):
    chunk_id: str = Field(..., description="The string representatio of the Mongo ObjectId")
    content: str = Field(..., description="The updated or newly inserted 1000-character max string")
    metadata: dict