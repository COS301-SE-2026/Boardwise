import logging
import math
from typing import Literal, Callable
from dataclasses import dataclass
from pydantic import BaseModel

from app.schemas.ggaia_schemas import (
    ComponentPool, Difficulty, 
    PlayerRange, ScaledGame, 
    ScaleOptions
)

from app.services.ggaia_new_game import (
    GenerationConfig,
    generate_checked,
    normalise_text
)

from app.retrieval.vector_store import fetch_candidate_chunks
from app.generation.ggaia_prompt import game_scaler
from app.utils.ggaia_utils import validate_game_against_pool, duplicate_component_overuse

logger = logging.getLogger(__name__)

class ScalingError(RuntimeError):
    reason = "scaling_failed"

class Unscalable(ScalingError):
    reason = "nothing to scale"

class RulesUnavailable(ScalingError):
    reason = "rulebook_missing"

class MissingStats(ScalingError):
    reason = "missing_game_statistics"

class MissingComponentData(ScalingError):
    reason = "missing_component_data"

@dataclass(frozen=True)
class ScalingBrief:
    base_player_count: tuple[int, int]
    target_player_count: tuple[int, int]
    player_case: Literal["in_range", "below", "above", "both"]
    below_min: int
    above_max: int
    base_weight: float | None
    target_weight: float | None
    difficulty_gap: int
    difficulty_shift: Literal["none", "light", "moderate"]

PLAYER_MAP = {
    "1-2": (1, 2),
    "3-4": (3, 4),
    "5-6": (5, 6),
    "7+": (7, 10)
}

DIFFICULTY_STEP = {
    "EASIER": -1,
    "SIMILAR": 0,
    "HARDER": 1
}

WEIGHT_STEP = 1.0

def construct_scaling_brief(stats: dict, options: ScaleOptions) -> ScalingBrief:
    min_players, max_players = stats.get("minPlayers"), stats.get("maxPlayers")

    target_min, target_max = PLAYER_MAP[options.player_range]
    under, over = max(0, min_players - target_min), max(0, max_players - target_max)
    player_case = (
        "in_range" if not under and not over
        else "both" if under and over else
        "below" if under else "above"
    )

    steps = DIFFICULTY_STEP[options.difficulty]
    weight = stats.get("weight") or None
    target_weight = None if weight is None else min(5.0, max(1.0, weight + steps * WEIGHT_STEP))
    diff = abs(steps) * WEIGHT_STEP if weight is None else abs(target_weight - weight)
    shift = "none" if diff == 0 else "light" if diff <= 0.5 else "moderate"

    return ScalingBrief(
        (min_players, max_players),
        (target_min, target_max),
        player_case,
        under,
        over,
        weight,
        target_weight,
        steps,
        shift
    )

def scale_feasiblility(brief: ScalingBrief) -> None:
    if brief.player_case == "in_range" and brief.difficulty_shift == "none":
        diff_str = ("the game is already at its easiest or hardest level"
                  if brief.difficulty_gap != 0 else "difficulty is unaffected.")
        raise Unscalable(
            f"The target {brief.target_player_count[0]}-{brief.target_player_count[1]} players is already supported and {diff_str}."
        )

def scaling_brief_str(brief: ScalingBrief) -> str:
    min_players, max_players = brief.base_player_count
    target_min, target_max = brief.target_player_count
    if brief.player_case == "in_range":
        players = (
            f"Players: the original supports {min_players}-{max_players}, which already covers the target of "
            f"{target_min}-{target_max}. No player scaling required."
        )
    else:
        gaps = []
        if brief.below_min:
            gaps.append(f"{brief.below_min} below the original minimum")
        if brief.above_max:
            gaps.append(f"{brief.above_max} above the original maximum")

        players = (
            f"Players: the original supports {min_players}-{max_players}. The variant must "
            f"support {target_min}-{target_max} which extends {" and ".join(gaps)}. It must work "
            "well across that entire range."
        )
    if brief.difficulty_shift == "none":
        difficulty = "Difficulty: remain the same."
    else:
        diff_dir = "easier" if brief.difficulty_gap < 0 else "harder"
        weights = (f" (about {brief.base_weight:.1f} -> {brief.target_weight:.1f}) on BGG's 1-5 weight scale)"
                   if brief.base_weight and brief.target_weight else "")
        scope = ("Adjust numbers and thresholds only; do not add or remove rules." 
                 if brief.difficulty_shift == "light" else "Add, remove or soften at most ONE rule layer, plus number tuning.")
        difficulty = f"Difficulty: make the game {diff_dir}{weights}. {scope}"
    return f"{players}\n{difficulty}" 

SCALE_QUERIES = [
    "setup layout starting hand starting resources players",
    "turn structure phases actions each player turn",
    "game end trigger winning scoring victory points",
    "two player solo variant number of players teams",
    "resource cost limit maximum penalty"
]
P_KEY, I_KEY = "pageNumber", "chunkIndex"
q_vectors: dict[str, list[float]] = {}

@dataclass
class RulesExcerpt:
    text: str
    chunk_ids: list[str]
    queries_covered: int
    ordered: bool

def get_original_rules(
    rulebook_id: str,
    embed: Callable[[str], list[float]],
    *,
    per_query: int = 4,
    max_chars: int = 10000
) -> RulesExcerpt:
    per_query_hits: list[list[dict]] = []
    for query in SCALE_QUERIES:
        if query not in q_vectors:
            q_vectors[query] = embed(query)
        hits = fetch_candidate_chunks(rulebook_id, query, q_vectors[query], limit=per_query)
        per_query_hits.append([chunk for chunk in hits 
                              if chunk.get("type") != "decorative" 
                              and chunk.get("content", "".strip)])
    picked: list[dict] = []
    seen: set[str] = set()
    total = 0
    for rank in range(per_query):
        for hits in per_query_hits:
            if rank >= len(hits):
                continue
            chunk = hits[rank]
            size = len(chunk['content'])
            if chunk['chunkId'] in seen or total + size > max_chars:
                continue
            seen.add(chunk['chunkId'])
            picked.append(chunk)
            total += size
    if not picked:
        raise RulesUnavailable(f"No rule text could be retrieved for the rulebook {rulebook_id}.")

    ordered = all(P_KEY in chunk and I_KEY in chunk for chunk in picked)
    if ordered:
        picked.sort(key=lambda ch: (ch[P_KEY], ch[I_KEY]))

    covered_queries = sum(1 for hits in per_query_hits if any(hit['chunkId'] in seen for hit in hits))
    return RulesExcerpt(
        text="\n\n".join(ch["content"].strip() for ch in picked),
        chunk_ids=[ch['chunkId'] for ch in picked],
        queries_covered=covered_queries,
        ordered=ordered
    )

MAX_RULE_CHANGES = 8
def issues_with_scaling(
    scaled: ScaledGame, 
    brief: ScalingBrief, 
    pool: ComponentPool, 
    rules: str
) -> list[str]:
    issues: list[str] = []

    s_min, s_max = scaled.new_player_count
    t_min, t_max = brief.target_player_count
    if s_min > t_min or s_max < t_max:
        issues.append(f"new_player_count is {s_min}-{s_max} but the variant must support {t_min}-{t_max}.")

    issues += validate_game_against_pool(scaled, pool)
    issues += duplicate_component_overuse(scaled, pool)

    norm_rules = normalise_text(rules)
    quoted = 0
    for change in scaled.rule_changes:
        quote = normalise_text(change.original_quote)
        if not quote:
            continue
        if quote in norm_rules:
            quoted += 1
        else:
            issues.append(f"original_quote not found in the original rules: \"{change.original_quote[:80]}\"."
                          " Copy it as is from the excerpts, or leave it empty for new rules.")

    if quoted < math.ceil(len(scaled.rule_changes) / 2):
        issues.append("At least half of the changes must quote the original rule they modify.")

    if len(scaled.rule_changes) > MAX_RULE_CHANGES:
        issues.append(f"{len(scaled.rule_changes)} changes is too many. Keep it a maximum of {MAX_RULE_CHANGES}, "
                      "changing only what the brief requires.")

    types = {ch.type for ch in scaled.rule_changes}
    if brief.player_case != "in_range" and "player_scaling" not in types:
        issues.append("The target range extends beyond the original player range but there is no player_scaling change.")
    if brief.player_case == "in_range" and "player_scaling" in types:
        issues.append("No player scaling was requested. Remove the player_scaling changes.")
    if brief.difficulty_shift != "none" and "difficulty" not in types:
        issues.append("A difficulty shift was requested but there is no difficulty change.")
    if brief.difficulty_shift == "none" and "difficulty"  in types:
        issues.append("No difficulty shift was requested. Remove the difficulty changes.")
    return issues

@dataclass
class ScaleInput:
    title: str
    rulebook_id: str
    stats: dict
    pool: ComponentPool

class ScaleResult(BaseModel):
    scaled: ScaledGame
    target_player_count: tuple[int, int]
    player_case: str
    difficulty: Difficulty
    base_weight: float | None
    target_weight: float | None
    source_chunk_ids: list[str]
    reviewed: bool
    notes: list[str]

def generate_scaled_game(
    input: ScaleInput,
    options: ScaleOptions,
    ml_models: dict,
    embed: Callable[[str], list[float]],
    config: GenerationConfig
) -> ScaleResult:
    if not input.pool.components:
        raise MissingComponentData("This game has no extracted components, so a variant cannot be generated nor validated.")
    brief = construct_scaling_brief(input.stats, options)
    scale_feasiblility(brief)

    excerpt = get_original_rules(input.rulebook_id, embed)
    notes: list[str] = []
    if excerpt.queries_covered < len(SCALE_QUERIES):
        notes.append(f"Only {excerpt.queries_covered} of {len(SCALE_QUERIES)} rule topics were found in the rulebook; parts "
                     "of the variant may rest on incomplete rules.")
    if not excerpt.ordered:
        notes.append("Chunks lacked page/position metadata, so excerpts are in relevance order.")

    hint = "Use only component_ids from the pool, and copy every original_quote exactly from the excerpts."
    scaled = generate_checked(
        game_scaler(input.title, excerpt.text, input.pool, scaling_brief_str(brief)),
        ScaledGame,
        lambda s: issues_with_scaling(s, brief, input.pool, excerpt.text),
        ml_models, backend=config.generator_model, phase=config.scaler,
        attempts=config.realizer_retries, structured_retries=config.structured_retries,
        hint=hint
    )

    return ScaleResult(
        scaled=scaled,
        target_player_count=brief.target_player_count,
        player_case=brief.player_case,
        difficulty=options.difficulty,
        base_weight=brief.base_weight,
        target_weight=brief.target_weight,
        source_chunk_ids=excerpt.chunk_ids,
        reviewed=False,
        notes=notes
    )

