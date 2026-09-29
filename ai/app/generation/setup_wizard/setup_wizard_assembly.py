import re
import uuid

from app.generation.setup_wizard.setup_wizard_generation import LLMStep
from app.generation.setup_wizard.setup_wizard_grounding import check_step_grounding
from app.generation.setup_wizard.setup_wizard_prompt import PHASE_LABELS, PHASE_ORDER

EXCERPT_LENGTH = 250 

def _norm_text(text: str) -> str:
    return re.sub(r"\s+"," ", text.strip().lower())

def dedupe_steps(steps_by_phase: dict[str, list[LLMStep]]) -> dict[str, list[LLMStep]]:
    """
    Drop steps whose instruction repeats an earlier one (across all phases).
    """

    seen: set[str] = set()
    out: dict[str, list[LLMStep]] = {}
    for phase_key in PHASE_ORDER:
        kept = []
        for step in steps_by_phase.get(phase_key,[]):
            key = _norm_text(step.instruction)
            if key in seen:
                continue
            seen.add(key)
            kept.append(step)
        out[phase_key] = kept
    return out

def _slugify(name: str)-> str:
    """
    Used as a way to establish a somewhat stable root for a component;
    a short random suffix is added to avoid collisions between unrelated components that happen to share a normalised name.
    Example Wood block => wood-block
    """

    slug = re.sub(r"[^a-z0-9]+","-",name.strip().lower()).strip("-")
    return slug or "component"

def _normalise_name(name:str) -> str:
    """
    Used only for de-duplication matching, not for display. Method strips a trailing 's' so 'galleys' => 'galley'
    """
    return name.strip().lower().rstrip("s")

def _iter_named_components(steps_by_phase: dict[str, list[LLMStep]]):
    return (
        comp
        for steps in steps_by_phase.values()
        for step in steps
        for comp in step.components
        if comp.name and comp.name.strip()
    )

def build_master_components(steps_by_phase: dict[str, list[LLMStep]]) -> tuple[list[dict],dict[str,str]]:
    """
    Scans every step's own `components` list and builds one deduplicated master list, keyed by normalized name. Returns (component_list, name_to_id_map).
    Quantity: first non-null value wins, since a shared component's quantity should be the same wherever it's mentioned; a None ambiguity is left as None rather than guessed.
    """

    by_norm_name: dict[str, dict] = {}
    name_to_id: dict[str, str] = {}

    for comp in _iter_named_components(steps_by_phase):
        norm = _normalise_name(comp.name)
        existing = by_norm_name.get(norm)
        if existing is None:
            comp_id = f"c_{_slugify(comp.name)}_{uuid.uuid4().hex[:4]}"
            by_norm_name[norm] = {
                "id": comp_id,
                "name": comp.name.strip(),
                "quantity": comp.quantity,
            }
            name_to_id[norm] = comp_id
        elif existing["quantity"] is None:
            existing["quantity"] = comp.quantity

    return list(by_norm_name.values()), name_to_id

def build_component_refs(step: LLMStep, name_to_id: dict[str, str]) -> list[dict]:
    refs = []
    for comp in step.components:
        if not comp.name or not comp.name.strip():
            continue
        norm = _normalise_name(comp.name)
        comp_id = name_to_id.get(norm)
        if comp_id:
            refs.append({"id": comp_id, "quantity": comp.quantity})
    return refs

def build_step_sources(step: LLMStep, chunks_by_index: dict[int, str]) -> list[dict]:
    sources = []
    for idx in step.source_chunks:
        content = chunks_by_index.get(idx, "")
        excerpt = " ".join(content.split())[:EXCERPT_LENGTH]
        sources.append({"chunk_index": idx, "excerpt": excerpt})
    return sources

def assemble_wizard_output(
    steps_by_phase: dict[str, list[LLMStep]],
    chunks_by_index: dict[int, str],
)-> dict:
    """
    Turns raw per-phase LLM output into the frontend-shaped
    {components, phases, summary} payload. Does not attach job/game/config.
    The job runner knows the rulebook and job state, this function only knows about steps and chunks).
    """

    steps_by_phase = dedupe_steps(steps_by_phase)
    
    components, name_to_id = build_master_components(steps_by_phase)

    phases =[]
    order = 0
    total_steps = 0
    verified_steps = 0

    for phase_key in PHASE_ORDER:
        steps = steps_by_phase.get(phase_key, [])
        if not steps:
            continue #skip/ omit empty phases 

        wizard_steps = []
        for step in steps:
            order +=1
            total_steps += 1
            failures = check_step_grounding(step,chunks_by_index)
            confidence = "verified" if not failures else "flagged"
            if confidence == "verified":
                verified_steps +=1

            wizard_steps.append({
                "id": f"s{order}",
                "order": order,
                "title": step.title,
                "instruction": step.instruction,
                "component_refs": build_component_refs(step, name_to_id),
                "scope": step.scope,
                "optional": step.optional,
                "confidence": confidence,
                "sources": build_step_sources(step, chunks_by_index)
            })

        phases.append({
            "id": phase_key,
            "label": PHASE_LABELS[phase_key],
            "steps": wizard_steps
        })

    summary = {
        "total_steps": total_steps,
        "estimated_minutes": max(1,round((60 + total_steps * 40)/60)),
        "verified_steps": verified_steps,
    }

    return {
        "components": components,
        "phases": phases,
        "summary": summary
    }