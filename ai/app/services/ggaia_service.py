import logging
import re
import json
from dataclasses import dataclass
from typing import Callable, Generic, Literal, Optional, Type, TypeVar
from pydantic import BaseModel, ValidationError

from app.schemas.ggaia_schemas import (
    DesignDraft, NewGame, Diagnosis, ComponentPool, Comparison, Mechanic, Flaw
)

from app.generation.llm import Backend, ModelUnavailableError, generate_text
from app.generation.ggaia_prompt import (
    game_ideator_new_game, game_realizer_new_game, game_realizer_revise,
    game_critic_new_game_diagnose, game_critic_new_game_compare, new_game_block
)
from app.utils.ggaia_utils import (
    draft_feasibility, validate_game_against_pool, duplicate_component_overuse
)

logger = logging.getLogger(__name__)
T = TypeVar("T", bound=BaseModel)

# Exceptions
class InfeasiblePair(RuntimeError):
    "The pool cannot support enough mechanics to design anything."

class StructuredOutputError(RuntimeError):
    "The model never produced JSON matching the schema."

class GenerationFailed(RuntimeError):
    "The model kept violating code-level checks."

class CriticUnavailable(RuntimeError):
    "No usable critic response (both remote and local models failed or the output never validated)."

@dataclass(frozen=True)
class Phase:
    max_tokens: int
    temperature: float

@dataclass(frozen=True)
class GenerationConfig:
    generator_model: Backend = "local"
    critic_model: Backend = "auto"
    ideator_retries: int = 3
    realizer_retries: int = 3
    structured_retries: int = 3
    game_revisions: int = 2
    min_feasible_mechanics: int = 3
    ideation: Phase = Phase(3000, 0.8)
    realization: Phase = Phase(6000, 0.4)
    revision: Phase = Phase(6000, 0.3)
    diagnosis: Phase = Phase(2000, 0.1)
    comparison: Phase = Phase(1200, 0.1)

@dataclass
class GenerationInput:
    parent_a: dict
    parent_b: dict
    pool: ComponentPool
    mechanics: list[Mechanic]
    parent_a_rulebook_exp: Optional[str] = None
    parent_b_rulebook_exp: Optional[str] = None
    
class GenerationResult(BaseModel):
    draft: DesignDraft
    newgame: NewGame
    flaws: list[Flaw]
    used_revisions: int
    reviewed: bool
    confidence: Literal['low', 'high']
    notes: list[str]
    critic_degraded: bool

class StructuredOutput(Generic[T]):
    value: T
    source: str

FENCE = re.compile(r"^```(?:json)?\s*|\s*```$")

def parse_model(raw: str, model: Type[T]) -> T:
    text = FENCE.sub("", raw.strip())
    try:
        return model.model_validate_json(text)
    except ValidationError as v_err:
        if not all(err['type'] == 'json_invalid' for err in v_err.errors()):
            raise # escalate as there was another reason for failure

    json_decoder = json.JSONDecoder()
    last_error: Exception | None = None

    for match in re.finditer(r"\{", text):
        try:
            decoded, _ = json_decoder.raw_decode(text, match.start())
        except json.JSONDecodeError:
            continue
        if not isinstance(decoded, dict):
            continue

        try:
            return model.model_validate(decoded)
        except ValidationError as v_err:
            last_error = v_err
    raise last_error or ValueError("No JSON object found in the model output.")

def generate_structured_output(
    messages: list[dict],
    model: Type[T],
    ml_models: dict,
    *,
    backend: Backend,
    phase: Phase,
    attempts: int
) -> StructuredOutput[T]:

    msgs = list(messages)
    last_error = "unknown"
    for att in range(1, attempts + 1):
        output = generate_text(
            msgs,
            ml_models,
            backend=backend,
            max_tokens=phase.max_tokens,
            temperature=phase.temperature,
            response_schema= model.model_json_schema()
        )
        try:
            return StructuredOutput(parse_model(output.text, model), output.source)
        except (ValidationError, ValueError) as v_err:
            last_error = str(v_err)[:600]
            logger.warning(
                f"{model.__name__} invalid from {output.source} backend "
                f"(attempt {att}/{attempts}): {last_error}"
            )
            msgs += [
                {"role": "assistant", "content": output.text},
                {"role": "user", "content": (
                    f"That output was invalid: {last_error}\n"
                    "Return ONLY the corrected JSON object matching the schema."
                )}
            ]
    raise StructuredOutputError(f"{model.__name__} failed validation: {last_error}")

def generate_checked(
    messages: list[dict],
    model: Type[T],
    check: Callable[[T], list[str]],
    ml_models: dict, 
    *,
    backend: Backend,
    phase: Phase,
    attempts: int,
    structured_retries: int,
    hint: str = ""
) -> T:
    msgs, problems = messages, []
    for attempt in range(1, attempts + 1):
        to_check = generate_structured_output(msgs, model, ml_models, 
                                              backend=backend, 
                                              phase=phase, 
                                              attempts=structured_retries).value
        problems = check(to_check)
        if not problems:
            return to_check
        
        logger.warning(
            f"{model.__name__} failed code checks "
            f"(attempt {attempt}/{attempts}): {problems}"
        )
        msgs += [
            {"role": "assistant", "content": to_check.model_dump_json()},
            {"role": "user", "content": (
                "Your output has the following problems:\n- " +
                "\n- ".join(problems) +
                (f"\n{hint}" if hint else "") +
                "\nReturn the full corrected JSON object."
            )}
        ]
    raise GenerationFailed(f"{model.__name__} invalid after exhausting all {attempts} attempts: {problems}")

def normalise_text(text: str):
    text = re.sub(r"\s+", " ", text).strip().strip("\"'“”‘’")
    return text.rstrip(".…").strip().lower()

def sectioned_new_game_block(game: NewGame, pool: ComponentPool) -> dict[str, str]:
    parts = re.split(r"^### (\w+)\n", new_game_block(game, pool), flags=re.MULTILINE)
    return {parts[i]: parts[i + 1] for i in range(1, len(parts) - 1, 2)}

def verify_flaws(game: NewGame, pool: ComponentPool, flaws: list[Flaw]) -> list[Flaw]:
    sections = {
        name: normalise_text(text) 
        for name, text in sectioned_new_game_block(game, pool).items()
    }

    valid: list[Flaw] = []
    for flaw in flaws:
        evidence_quote = normalise_text(flaw.evidence_quote)
        if not evidence_quote:
            continue
        if evidence_quote in sections.get(flaw.section, ""):
            valid.append(flaw)
            continue
        actual_section = next((name for name, text in sections.items() if evidence_quote in text), None)
        if actual_section:
            corrected_flaw = flaw.model_copy(update={"section": actual_section})
            valid.append(corrected_flaw)
        else:
            logger.info(f"Unverifiable quote, dropped as a result: {flaw.evidence_quote[:80]}")

    return valid

class Critic:
    def __init__(self, ml_models: dict, config: GenerationConfig):
        self.ml_models = ml_models
        self.config = config
        self.self_critic = False

    def generate_completion(self, messages: list[dict], model: Type[T], phase: Phase) -> T:
        try:
            output = generate_structured_output(messages, model, self.ml_models, 
                                                backend=self.config.critic_model, phase=phase,
                                                attempts=self.config.structured_retries)
        except (StructuredOutputError, ModelUnavailableError) as err:
            raise CriticUnavailable(str(err)) from err
        
        if output.source != "remote":
            self.self_critic = True
        return output.value

    def diagnose(
        self, 
        draft: DesignDraft, 
        game: NewGame, 
        pool: ComponentPool,
        mechanics_by_id: dict[str, Mechanic]) -> list[Flaw]:
        diagnosis = self.generate_completion(
            game_critic_new_game_diagnose(draft, game, pool, mechanics_by_id),
            Diagnosis,
            self.config.diagnosis
        )
        return verify_flaws(game, pool, diagnosis.flaws)

    def compare(
        self,
        draft: DesignDraft,
        pool: ComponentPool,
        mechanics_by_id: dict[str, Mechanic],
        current: NewGame,
        candidate: NewGame
    ) -> bool:
        cand_over_curr = curr_over_cand = 0
        for cand_first in (False, True):
            v1, v2 = (candidate, current) if cand_first else (current, candidate)
            comparison = self.generate_completion(
                game_critic_new_game_compare(draft, v1, v2, pool, mechanics_by_id),
                Comparison,
                self.config.comparison
            )
            if comparison.preferred == 'tie':
                continue
            if (comparison.preferred == 'version_1') == cand_first:
                cand_over_curr += 1
            else:
                curr_over_cand += 1
        return cand_over_curr >= 1 and curr_over_cand == 0

def generate_new_game(
    input: GenerationInput,
    ml_models: dict, 
    config: GenerationConfig = GenerationConfig()
) -> GenerationResult:
    pool = input.pool
    mechs_by_id = {m.mechanic_id: m for m in input.mechanics}
    notes: list[str] = []

    feasible = [m for m in input.mechanics if pool.mechanic_feasibility(m)]
    if len(feasible) < config.min_feasible_mechanics:
        raise InfeasiblePair(
            f"Only {len(feasible)} of {len(input.mechanics)} candidate mechanics are"
            f" feasible with this pool."
        )

    draft = generate_checked(
        game_ideator_new_game(
            input.parent_a, 
            input.parent_b,
            [m.model_dump() for m in feasible],
            pool.available_types()
        ),
        DesignDraft,
        lambda d: draft_feasibility(d, pool, mechs_by_id),
        ml_models,
        backend=config.generator_model,
        phase=config.ideation,
        attempts=config.ideator_retries,
        structured_retries=config.structured_retries,
        hint="Only use mechanic_ids present in the candidate list."
    )

    def new_game_issues(game: NewGame):
        validate_game_against_pool(game, pool) + duplicate_component_overuse(game, pool)

    new_game_hint = "Only use component_ids present in the pool. Never more than the available quantity."

    new_game = generate_checked(
        game_realizer_new_game(draft, pool, 
                               mechs_by_id, 
                               input.parent_a_rulebook_exp, 
                               input.parent_b_rulebook_exp),
        NewGame, new_game_issues, ml_models, backend=config.generator_model,
        phase=config.realization, attempts=config.realizer_retries, structured_retries=config.structured_retries,
        hint=new_game_hint
    )

    critic = Critic(ml_models, config)
    flaws: list[Flaw] = []
    reviewed = False
    used_revisions = 0
    try:
        # implements algo 1 from autobg
        for revision in range(config.game_revisions + 1):
            # reset the flaws and reviewed vars per iteration
            reviewed = False
            flaws = []

            flaws = critic.diagnose(draft, new_game, pool, mechs_by_id)
            reviewed = True
            if not flaws:
                break
            if revision == config.game_revisions:
                notes.append("Maximum revisions exhausted; unresolved flaws reported.")
                break

            try:
                candidate = generate_checked(
                    game_realizer_revise(draft, pool, mechs_by_id, new_game, flaws),
                    NewGame, new_game_issues, ml_models, backend=config.generator_model,
                    phase=config.revision, attempts=config.realizer_retries, 
                    structured_retries=config.structured_retries,
                    hint=new_game_hint
                )
            except (GenerationFailed, StructuredOutputError, ModelUnavailableError) as err:
                notes.append(f"Revision no.{revision + 1} did not produce a valid game: {err}")
                break

            if critic.compare(draft, pool, mechs_by_id, new_game, candidate):
                new_game = candidate
                used_revisions += 1
            else: # no improvement
                notes.append(f"Revision no.{revision + 1} rejected by critic comparison; Previous version retained.")
                break


    except CriticUnavailable as err:
        notes.append(f"Critic unavailable; new game not fully reviewed: {err}")

    if critic.self_critic:
        notes.append("Somewhere during the review the local model was ran as the critic (Decreased reliability on output).")

    confident = reviewed and not critic.self_critic and not any(flaw.flaw_type.endswith('_critical') for flaw in flaws)
    
    return GenerationResult(
        draft=draft,
        newgame=new_game,
        flaws=flaws,
        used_revisions=used_revisions,
        reviewed=reviewed,
        confidence="high" if confident else "low",
        notes=notes,
        critic_degraded=critic.self_critic
    )