from datetime import datetime

from typing import Literal, Optional
from pydantic import BaseModel, ConfigDict, Field, model_validator
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
    chunk_id: str = Field(..., description="The string representation of the Mongo ObjectId")
    content: str | None = Field(None, description="The updated or newly inserted 1000-character max string")
    metadata: dict | None = None

class GGAIARequest(BaseAPIModel):
    type: Literal["SCALE", "NEW"] = Field(
        ..., 
        description=(
            "Determines the type of generation job that should be ran. "
            "\"SCALE\" would be for scaling a game's difficulty and/or player count. "
            "\"NEW\" would be for generating a new game from two source games"
        )
    )
    parents: list[str] | Literal["Surprise Me"] = Field(
        ...,
        description=(
            "Represents the games to be used for the generation step. it should "
            "be at most be of length 2 (particularly for the \"NEW\" request type). "
            "With regards to \"NEW\" requests, the value will be of string \"Surprise Me\" "
            "when the client wants the application to pick games for them. Parents should be of length 1 "
            "for the \"SCALE\" request type."
        )
    )
    scale_options: Optional[dict] = Field(
        None,
        description=(
            "Should be None for \"NEW\" request type, "
            "otherwise should contain the options for scaling."
        )
    )

    @model_validator(mode="after")
    def validate(self):
        if self.type == "NEW":
            if isinstance(self.parents, list) and len(self.parents) != 2:
                raise ValueError("The \"parents\" field must be of length two if parent games are supplied.")
            
            elif isinstance(self.parents, str) and self.parents.lower() != "Surprise Me".lower():
                raise ValueError("If \"parents\" is not an array, then it must be \"Surprise Me\".")

            elif self.scale_options is not None:
                raise ValueError("The \"scale_options\" field must not be set for requests of type \"NEW\".")
            
        elif self.type == "SCALE":
            if not isinstance(self.parents, list):
                raise ValueError("The \"parents\" field must be an array.")
            
            elif len(self.parents) != 1:
                raise ValueError("The \"parents\" field must be of length one.")
            
            elif self.scale_options is None:
                raise ValueError("The \"scale_options\" field must be set for requests of type \"NEW\".")

        return self





    
