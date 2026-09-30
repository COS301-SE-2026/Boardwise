from typing import Any, Literal, List, Tuple, Optional
from pydantic import BaseModel, Field, model_validator, field_validator
from app.schemas.schemas import BaseAPIModel

ComponentType = Literal[
    "board", "card", "die", "token", "meeple", "tile", "miniature", "other"
]


class Component(BaseModel):
    component_id: str
    type: ComponentType
    name: str
    quantity: int
    attributes: dict[str, Any]

class Mechanic(BaseModel):
    mechanic_id: str
    name: str
    category: str
    description: str
    requires_component_types: list[ComponentType]
    min_players: int
    max_players: int


# "Helper" classes for design draft
class Concept(BaseModel):
    elevator_pitch: str = Field(
        ...,
        description="1 sentence summary of the design's core, 15-30 words."
    )
    description: str = Field(
        ...,
        description="Design concept from a designer's perspective, 200-400 words."
    )

GameType = Literal[
    "Abstract Strategy Games",
    "Customisable Games",
    "Thematic Games",
    "Family Games",
    "Children's Games",
    "Party Games",
    "Strategy Games",
    "Wargames"
]

class Classification(BaseModel):
    type: List[GameType] = Field(
        ..., 
        description=(
            "Choose one or more of the defined game types. "
            "Draw inspiration from the parent games if necessary"
        )
    )
    genres: List[str] = Field(
        ...,
        description=(
            "Genre tags, drawn from BGG categories. possibly draw inspiration from parents"
        )
    )

MECHANIC_ID_DESC = "Must be an exact mechanic_id from the mechanics made available to you."

class CoreMechanic(BaseModel):
    mechanic_id: str = Field(
        ...,
        description=MECHANIC_ID_DESC
    )
    rationale: str = Field(
        ...,
        description=(
            "Why this is a mechanic is chosen as core and "
            "how it forms the main decision loop. 1-3 sentences."
        )
    )

class SupportingMechanic(BaseModel):
    mechanic_id: str = Field(
        ...,
        description=MECHANIC_ID_DESC
    )
    rationale: str = Field(
        ..., 
        description=(
            "How the chosen mechanic serves the core gameplay and which core "
            "mechanic it relates to, 1-2 sentences."
        )
    )

class StructuralMechanic(BaseModel):
    mechanic_id: str = Field(
        ...,
        description=MECHANIC_ID_DESC
    )
    rationale: str = Field(
        ...,
        description="What framework or setup property the mechanic describes, 1 sentence."
    )

class Mechanics(BaseModel):
    core: List[CoreMechanic] = Field(
        ...,
        min_length=1,
        description="Mechanics that form the main decision loop."
    )
    supporting: List[SupportingMechanic] = Field(
        default_factory=list,
        description="Mechanics that reinforce or extend the core loop."
    )
    structural: List[StructuralMechanic] = Field(
        default_factory=list,
        description="Framework/setup mechanics (turn order, board layout, etc.)."
    )

class DesignIntent(BaseModel):
    target_experience: str = Field(
        ...,
        description=(
            "The core experience and emotions the game creates for players, "
            "2-3 sentences."
        )
    )
    core_tension: str = Field(
        ...,
        description=(
            "The central dilemma or trade-off players face. "
            "Be specific, 1-2 sentences."
        )
    )
    theme_mechanic_fit: str = Field(
        ...,
        description=(
            "How the theme (genres) and core mechanics reinforce "
            "each other, 2-3 sentences."
        )
    )

# based on BGG
LanguageDependency = Literal[
    "No necessary in-game text",
    "Some necessary text",
    "Moderate in-game text",
    "Extensive use of text",
    "Unplayable in another language"
]

class Parameters(BaseModel):
    complexity: float = Field(
        ..., 
        ge=1,
        le=5,
        description=(
            "Complexity on a scale from 1-5. Following from the selected mechanics; "
            "parent values are for reference only."
        )
    )
    player_count: Tuple[int, int] = Field(
        ..., 
        description=(
            "[min, max] supported player count. Must be feasible with "
            "component pool supplied."
        )
    )
    play_time_in_minutes: Tuple[int, int] = Field(
        ..., 
        description="[min, max] estimated play time, in minutes."
    )
    recommended_players: str = Field(
        ...,
        description="The best and recommended player count(s)."
    )
    language_dependency: LanguageDependency = Field(
        ...,
        description="Level of language dependency in this game."
    )
    parameters_rationale: str = Field(
        ...,
        description=(
            "How the complexity, player count, play time and language "
            "dependency form a coherent design package, 2-3 sentences."
        )
    )

    @field_validator("player_count", "play_time_in_minutes")
    @classmethod
    def min_and_max_valid(cls, value: Tuple[int, int]):
        minimum, maximum = value
        if minimum > maximum:
            raise ValueError(f"Minimum value ({minimum}) cannot be greater than maximum value ({maximum})")
        return value

class DesignDraft(BaseModel):
    concept: Concept
    classification: Classification
    mechanics: Mechanics
    design_intent: DesignIntent
    parameters: Parameters

    @model_validator(mode="after")
    def mechanic_ids_unique(self) -> "DesignDraft":
        ids = (
            [m.mechanic_id for m in self.mechanics.core] +
            [m.mechanic_id for m in self.mechanics.supporting] +
            [m.mechanic_id for m in self.mechanics.structural]
        )
        if len(ids) != len(set(ids)):
            raise ValueError(
                "A mechanic_id is repeated in either core, supporting or structural."
            )
        return self

# "Helper" classes for New Game
class FAQEntry(BaseModel):
    question: str = Field(
        ...,
        description="A rules question players are likely to ask."
    )
    answer: str = Field(
        ...,
        description="A direct and concise answer, 1-3 sentences."
    )

class ComponentEntry(BaseModel):
    component_id: str = Field(
        ...,
        description=(
            "Must match an exact component_id from the component pool made available to you."
        )
    )
    quantity: int = Field(
        ...,
        ge=1,
        description="How many of this component the game uses."
    )
    notes: str = Field(
        default="",
        description=(
            "Optional short notes about the component's role or any variant "
            "(e.g. 'used as currency', 'one per player')."
        )
    )

class NewGame(BaseModel):
    lore_and_objective: str = Field(
        ...,
        description=(
            "The game's theme and/or setting framing and the player's overall objective. "
            "This acts as the opening of the rulebook."
        )
    )

    components: List[ComponentEntry] = Field(
        ...,
        min_length=1,
        description=(
            "The list of components needed to play this game. Drawn from the "
            "Design draft's parameters -- not copied from any reference game."
        )
    )

    setup: str = Field(
        ...,
        description=(
            "Step-by-step instructions setting up the game before the first turn occurs "
            "(board/grid layout, dealing, starting positions)."
        )
    )

    gameplay_flow: str = Field(
        ...,
        description=(
            "Turn/round structure: what occurs each turn, in what order it occurs, "
            "across a full round. Must be sequential and unambiguous, as this is where "
            "the dynamics-layer flaws occur the most."
        )
    )

    core_mechanics: str = Field(
        ...,
        description=(
            "How the design draft's core/supporting mechanics actually operate as rules"
            " (the detailed 'how to do X' rules the gameplay_flow section references)."
        )
    )

    scoring_and_endgame: str = Field(
        ...,
        description=(
            "How the game ends, and how a winner is determined. Must be "
            "resolvable given the components and rules defined above."
        )
    )

    faq: List[FAQEntry] = Field(
        default_factory=list,
        description=(
            "Anticipated rules questions and answers, covering edge cases not obvious "
            "from the main sections."
        )
    )

class ComponentPool(BaseModel):
    source_game_ids: list[str] = Field(
        ...,
        description=(
            "Mongodb assigned ids of the games selected by the user "
            "from which this pool was drawn from."
        )
    )
    components: List[Component] = Field(
        ...,
        description=(
            "Every component across all source games."
        )
    )

    def available_types(self) -> set[ComponentType]:
        return {comp.type for comp in self.components}
    
    def quantity_per_type(self, type: ComponentType) -> int:
        return sum(comp.quantity for comp in self.components if comp.type == type)

    def mechanic_feasibility(self, mechanic: Mechanic) -> bool:
        return all(
            self.quantity_per_type(type) > 0 
            for type in mechanic.requires_component_types
        )

    def get_component_by_id(self, component_id: str) -> Component | None:
        return next(
            (comp for comp in self.components 
             if comp.component_id == component_id), None
        )

    def component_count_feasible(self, component_id: str, needed_comp_count: int) -> bool:
        component = self.get_component_by_id(component_id)
        return component is not None and component.quantity >= needed_comp_count

# Critic schemas
FlawType = Literal[
    "M_critical", "M_major", "M_minor",
    "D_critical", "D_major", "D_minor",
    "A_major", "A_minor"
]

NewGameSection = Literal[
    "lore_and_objective", "components", "setup", 
    "gameplay_flow", "core_mechanics", "scoring_and_endgame",
    "faq"
]

class Flaw(BaseModel):
    flaw_type: FlawType = Field(
        ...,
        description="Exactly one flaw type from the taxonomy in the instructions."
    )
    section: NewGameSection = Field(
        ...,
        description="The New Game section that contains your evidence quote."
    )
    evidence_quote: str = Field(
        ...,
        description=(
            "A passage of at most one sentence, copied from that section VERBATIM, "
            "character for character. If the flaw is a missing rule, quote the sentence"
            " where the gap becomes immediately apparent."
            
        )
    )

    affected_target: str = Field(
        ...,
        description="The actual rule, mechanic or component this flaw affects/concerns."
    )
    problem: str = Field(..., description="What is wrong, 1-2 sentences.")
    mda_chain: str = Field(
        ...,
        description=(
            "1-2 sentences tracing the consequence: how the flawed rule changes what "
            "happens during the play, and how that hurts the player experience."
        )
    )
    repair_suggestions: str = Field(
        ...,
        description="A specific, actionable fix, 1-2 sentences."
    )

class Diagnosis(BaseModel):
    flaws: List[Flaw] = Field(
        max_length=3,
        description=(
            "Genuine flaws, most severe first. An empty list means the new game contains "
            "no flaws worth fixing."
        ),
        default_factory=list
    )

class Comparison(BaseModel):
    version_1_flaws: List[str] = Field(
        default_factory=list,
        max_length=3,
        description=(
            "Genuine flaws in version 1, most severe first. One short line each."
        )
    )
    version_2_flaws: List[str] = Field(
        default_factory=list,
        max_length=3,
        description=(
            "Genuine flaws in version 2, most severe first. One short line each."
        )
    )
    reasoning: str = Field(
        ..., 
        description="1-2 sentences weighting the flaws above by severity."
    )
    preferred: Literal["version_1", "version_2", "tie"]

class RuleChange(BaseModel):
    type: Literal["player_scaling", "difficulty"] = Field(
        ...,
        description="Whether this change serves the target player count or the difficulty change."
    )
    section: NewGameSection = Field(
        ...,
        description="The rules section this change applies to."
    )
    original_quote: str = Field(
        ...,
        description=(
            "A passage of at most one sentence copied VERBATIM from the original rules that this "
            "change modifies. Leave this empty ONLY if this change adds a new rule."
        )
    )
    change: str = Field(
        ..., 
        description="The new or modified rule, written for players to follow. 2-3 sentences."
    )
    reason: str = Field(
        ...,
        description="How this serves the target player count or difficulty. 1 sentences."
    )

class ScaledGame(BaseModel):
    new_player_count: Tuple[int, int] = Field(
        ...,
        description="[min, max] player count this version supports."
    )
    version_summary: str = Field(
        ...,
        description="What changes and what stays the same, 2-3 sentences."
    )
    components: List[ComponentEntry] = Field(
        ...,
        min_length=1,
        description="The COMPLETE component list for this version (not just the differences), from the pool."
    )
    rule_changes: List[RuleChange] = Field(..., min_length=1)
    version_faq: List[FAQEntry] = Field(default_factory=list)

    @field_validator("new_player_count")
    @classmethod
    def min_and_max_valid(cls, value: Tuple[int, int]):
        minimum, maximum = value
        if minimum > maximum:
            raise ValueError(f"Minimum value ({minimum}) cannot be greater than maximum value ({maximum})")
        return value


# --- Over the wire stuff ig ---
PlayerRange = Literal["1-2", "3-4", "5-6", "7-10"]
Difficulty = Literal["EASIER", "SIMILAR", "HARDER"]

class ScaleOptions(BaseAPIModel):
    player_range: PlayerRange
    difficulty: Difficulty

class GGAIARequest(BaseAPIModel):
    type: Literal["SCALE", "NEW"]
    parents: list[str] | Literal["Surprise Me"]
    scale_options: Optional[ScaleOptions] = None

    @field_validator("parents", mode="before")
    @classmethod
    def correct_surprise_me(cls, value):
        if isinstance(value, str) and value.strip().lower() == "surprise me":
            return "Surprise Me"
        return value
    
    @model_validator(mode="after")
    def validate_self(self):
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
                raise ValueError("The \"scale_options\" field must be set for requests of type \"SCALE\".")

        return self