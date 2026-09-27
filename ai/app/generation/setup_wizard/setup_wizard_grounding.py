from setup_wizard_generation import LLMStep
def check_step_grounding(step: LLMStep, chunks_by_index: dict[int, str]) -> list[str]:
    reasons: list[str] = []
    if not step.title.strip():
        reasons.append("title field is empty")
    if not step.instruction.strip():
        reasons.append("instruction field is empty")

    for idx in step.source_chunks:       
        if idx not in chunks_by_index:
            reasons.append(f"cited chunk {idx} was never retrieved")
            continue
        chunk_text = chunks_by_index[idx].lower()
        if step.components:
            missing = [c.name for c in step.components
                       if c.name and c.name.lower() not in chunk_text
                       and c.name.lower().rstrip("s") not in chunk_text]
            if missing:
                reasons.append(f"component name(s) not found in chunk {idx}: {missing}")
            bad_quantity = [c.name for c in step.components
                             if c.quantity is not None and str(c.quantity) not in chunk_text]
            if bad_quantity:
                reasons.append(f"quantity for {bad_quantity} not found in chunk {idx}")
    return reasons


def check_grounding(steps_by_phase: dict[str, list[LLMStep]], chunks_by_index: dict[int, str]) -> list[dict]:
    failures = []
    seen_titles: dict[str, str] = {}
    for phase, steps in steps_by_phase.items():
        for step in steps:
            for reason in check_step_grounding(step, chunks_by_index):
                failures.append({"phase": phase, "step": step.title, "reason": reason})
            key = step.title.strip().lower()
            if key:
                if key in seen_titles and seen_titles[key] != phase:
                    failures.append({"phase": phase, "step": step.title,
                                      "reason": f"duplicate of a step already in phase '{seen_titles[key]}'"})
                seen_titles.setdefault(key, phase)
    return failures  