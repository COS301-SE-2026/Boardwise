#Setup Wizard models
from pydantic import Field
from pydantic.alias_generators import to_camel
from datetime import datetime
from typing import Literal 
from .schemas import BaseAPIModel
from utils.pyObjectId import PyObjectId

PHASE_LABELS={
    "board": "Build the board",
    "components": "Sort the components",
    "players": "Prepare each player",
    "final": "Final steps",
}

PhaseKey = Literal["board", "components", "players", "final"]
Scope = Literal["shared", "per_player"]
WarningType = Literal["ambiguous_rule", "missing_info", "conflicting_rule"]


class LLMComponentUse(BaseAPIModel):
    name: str
    quantity: int | None = None

class LLMStep(BaseAPIModel):
    title: str = Field(..., max_length = 80)
    instruction: str  = Field(..., max_length = 400)
    components: list[LLMComponentUse] = []
    scope: Scope
    optional: bool = False
    source_chunks: list[int] = Field(...,min_length=1) #chunk index

class LLMPhase(BaseAPIModel):
    key: PhaseKey # board | components | players | final
    steps: list[LLMStep]

class LLMWarning(BaseAPIModel):
    type: WarningType # ambiguous_rule | missing_info | conflicting_rule
    message: str
    related_step_number: int | None = None # 1-based position across all steps in output order

class LLMSetupOutput(BaseAPIModel):
    components: list[LLMComponentUse]
    phases: list[LLMPhase]
    warnings: list[LLMWarning] = []


class SetupWizardRequest(BaseAPIModel):
    rulebook_id: str

class WizardJob(BaseAPIModel):
    id: str 
    status: Literal["queued", "running", "ready", "failed"]
    progress: int = Field(0, ge=0, le=100)
    generated_at: datetime | None = None
    error: str | None = None

class WizardGame(BaseAPIModel):
    id: str
    name: str
    rulebook_id: str
    rulebook_version: int | None = None 


class WizardConfig(BaseAPIModel):
    min_players: int
    max_players: int

class WizardSummary(BaseAPIModel):
    total_steps: int
    estimated_minutes: int
    verified_steps: int


class WizardComponent(BaseAPIModel):
    id: str
    name: str
    quantity: int | None = None


class ComponentRef(BaseAPIModel):
    id: str
    quantity: int | None = None


class StepSource(BaseAPIModel):
    chunk_index: int
    excerpt: str


class WizardStep(BaseAPIModel):
    id: str
    order: int
    title: str
    instruction: str
    component_refs: list[ComponentRef] = []
    scope: Scope
    optional: bool
    confidence: Literal["verified", "unverified", "flagged"]
    sources: list[StepSource]


class WizardPhase(BaseAPIModel):
    id: PhaseKey
    label: str
    steps: list[WizardStep]


class WizardWarning(BaseAPIModel):
    type: WarningType
    message: str
    related_step_id: str | None = None
    suggested_question: str | None = None

# For Frontend
class SetupWizardResponse(BaseAPIModel):
    schema_version: int = 1 # version tracking
    job: WizardJob
    game: WizardGame | None = None       # None until ready
    config: WizardConfig
    summary: WizardSummary | None = None
    components: list[WizardComponent] = []
    phases: list[WizardPhase] = []
    warnings: list[WizardWarning] = []

# MODEL
class WizardDocument(BaseAPIModel):
    id: PyObjectId = Field(alias="_id")
    rulebook_id: str
    created_at: datetime
    updated_at: datetime
    
    schema_version: int = 1
    job: WizardJob
    game: WizardGame | None = None
    config: WizardConfig
    summary: WizardSummary | None = None
    components: list[WizardComponent] = []
    phases: list[WizardPhase] = []
    warnings: list[WizardWarning] = []