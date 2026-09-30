import json
from typing import Optional, Sequence
from pydantic import BaseModel
from app.schemas.ggaia_schemas import (
    DesignDraft, 
    NewGame, 
    ComponentPool,
    Mechanic,
    ComponentEntry,
    Comparison,
    Diagnosis,
    CoreMechanic,
    SupportingMechanic,
    StructuralMechanic,
    Flaw,
    ScaledGame
)

#-------- build ggaia generation prompts ---------
def schema_json(model: type[BaseModel]) -> str:
    return json.dumps(model.model_json_schema(), separators=(",", ":"))

# new game 
def generate_parent_string(parent: dict):
    parent_types = ", ".join(parent['types']) if parent['types'] else "No classified type"
    parent_genres = ", ".join(parent['genres'])
    text = (
        f"{parent['title']} ({parent_types}; genres: {parent_genres})\n"
        f" {parent['description']}"
    )
    stats = parent.get("stats")
    if stats:
        stats_parts = []
        if stats.get("weight"):
            stats_parts.append(f"complexity: {stats['weight']}/5")

        if stats.get("minPlayers") and stats.get("maxPlayers"):
            stats_parts.append(f"players: {stats['minPlayers']}-{stats['minPlayers']}")

        if stats.get("minDuration") and stats.get("maxDuration"):
                    stats_parts.append(f"Duration: {stats['minDuration']}-{stats['minDuration']}")
        if stats_parts:
            text += f"\n Values for reference:\n {", ".join(stats_parts)}"
    return text
        
def _mechanic_line(mechanic_id: str, name: str, category: str, description: str, requires: list[str]) -> str:
    cat = f"({category})" if category else ""
    req = ", ".join(requires) or "no specific components"
    return f"- id={mechanic_id} | {name}{cat}: {description} [requires: {req}]"

def game_ideator_new_game(
    parent_a: dict,
    parent_b: dict,
    potential_mechanics: list[dict],
    pool: ComponentPool
) -> list[dict]:
    system_prompt = """You are a board game design analyst. You will be given two
"parent" games and a pool of physical components available to build with. Your job
is to design a SINGLE new game that meaningfully combines ideas from both parents, using
only mechanics that are physically feasible given the component pool.

Rules:
- Give the new game an original title
- Every mechanic you select (core, supporting or structural) MUST come from the provided
  candidate mechanics list. Reference mechanics by their mechanic_id.
- Before selecting a mechanic, check that every component type it requires is present in the
  available component types. Never select an infeasible mechanic.
- The two parents may have different, overlapping or no classified types at all -- pick 
  one or more classification types that best fit the NEW design on its own merits.
- Synthesize a coherent theme for the new design; you are not required to reuse either parent's theme verbatim,
  but the result should feel intentional, not like two settings frankensteined together.
- Every mechanic's rationale must explain its role in THIS new design.
- Any \"values for reference\" given for the parents are context and not targets. Choose the new 
  design's complexity, player count and play time so that they follow from the mechanics you select and 
  from the component quantities in the pool. Do not simply average the parents' values. The player count 
  must not exceed what the pooled components can physically support.

Respond with ONLY a JSON object matching the provided schema. No other text.
"""

    mechanic_block = "\n".join(
        _mechanic_line(m['mechanic_id'], m['name'], m['category'], m['description'], m['requires_component_types'])
        for m in potential_mechanics
    )

    available_components_str = ", ".join(
        f"{type} (x{pool.quantity_per_type(type)})"
        for type in sorted(pool.available_types())
    )

    user_prompt = f"""## Parent A
{generate_parent_string(parent_a)}

## Parent B
{generate_parent_string(parent_b)}

## Candidate Mechanics (select ONLY from these, by mechanic_id)
{mechanic_block}

## Available Component Types in the Pool
{available_components_str}

## Task
Design one new game drawing on ideas from both parents, using only mechanics whose required component type
are present in the pool. Produce a single JSON object matching this schema:
{schema_json(DesignDraft)}
"""
    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

def format_pool_block(pool: ComponentPool) -> str:
    lines = []
    for component in pool.components:
        line = (
            f"- component_id={component.component_id} | {component.name} ({component.type}), "
            f"available quantity: {component.quantity}"
        )
        if component.attributes:
            line += f", attributes: {json.dumps(component.attributes)}"
        lines.append(line)

    return "\n".join(lines)

def game_realizer_new_game(
    draft: DesignDraft, 
    pool: ComponentPool,
    mechanics_by_id: dict[str, Mechanic],
    parent_a_rulebook_exp: Optional[str] = None,
    parent_b_rulebook_exp: Optional[str] = None,
) -> list[dict]:

    system_prompt = """You are an expert board game rulebook author. Given a
design draft (mechanics, theme, design intent and parameters) and a pool of
specific physical components, you must write a complete rulebook as a single JSON 
object matching the provided schema.

Critical requirements:
- Every component you reference in the "components" field MUST be an exact component_id
  from the component pool provided below. Never invent a component_id and never request more
  of a component than the pool actually contains.
- Choose component quantities appropriate to the draft's complexity and player count -- but 
  only from what the pool makes available. If the ideal design calls for more of a component
  than the pool has, adapt the design (e.g. adjust scaling or how a mechanic is used) rather
  than exceeding the pool.
- Player count scaling must be explicit wherever it affects setup, component counts or any 
  variable rule.
- gameplay_flow and core_mechanics must faithfully implement every core and supporting mechanic
  listed in the design draft -- no mechanic may be silently dropped.
- faq must address genuine ambiguities that could arise from THIS specific rulebook, not generic
  rules questions.
- Write for a first-time player: clear, precise and unambiguous.

Writing style for the prose fields (lore_and_objective, setup, gameplay_flow, core_mechanics,
scoring_and_endgame):
- Write as flowing prose paragraphs, not bullet-point lists.
- Use numbered steps ONLY for strictly sequential procedures (setup steps, turn phase order within 
  gameplay_flow) -- these are the parts most likely to contain a sequencing error, so make the order
  explicit and unambiguous.
- Match the tone and density of a polished, professionally published rulebook.

This design draws on two parent games. Do not interleave their rules one after another -- synthesise a single,
unified ruleset that reads as one coherent game.

Respond with ONLY a JSON object matching the provided schema. No other text. Represent line breaks as '\\n' in
JSON string values.
"""
    
    chosen_mechanics = (
        [m.mechanic_id for m in draft.mechanics.core] +
        [m.mechanic_id for m in draft.mechanics.supporting] +
        [m.mechanic_id for m in draft.mechanics.structural]
    )

    mechanic_block = "\n".join(
        f"- id={mid} | {mechanics_by_id[mid].name}: {mechanics_by_id[mid].description}"
        for mid in chosen_mechanics
        if mid in mechanics_by_id
    )

    guidance_block = ""
    if parent_a_rulebook_exp or parent_b_rulebook_exp:
        guidance_block = f"""## Reference Rulebook Excerpts (depth/structure calibration ONLY)
Use these only to gauge expected level of detail. Do NOT copy their component picks, quantities or
numeric values -- you are constrained only to the pool below.

-- Parent A excerpt --
{parent_a_rulebook_exp or "(Not available)"}

-- Parent B excerpt --
{parent_b_rulebook_exp or "(Not available)"}
"""

    user_prompt = f"""{guidance_block}
## Design Draft
{draft.model_dump_json(indent=2)}

## Mechanics used in the draft
{mechanic_block}

## Component Pool (select ONLY from these, by component_id)
{format_pool_block(pool)}

## Task
Write the complete rulebook for the design draft above. Produce a single JSON object
matching this schema:
{schema_json(NewGame)}
"""
    
    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

def game_realizer_revise(
    draft: DesignDraft,
    pool: ComponentPool,
    mechanics_by_id: dict[str, Mechanic],
    current: NewGame,
    flaws: list[Flaw]
) -> list[dict]:
    system_prompt = """You are an expert board game rulebook author revising an existing rulebook. 
A reviewer found specific flaws. Produce a corrected version of the FULL rulebook as a single JSON 
object matching the provided schema.

Rules:
- Fix every listed flaw. Change only what is needed to fix them; keep all other text and structure as 
  close to the current version as possible.
- Do not drop, rename or weaken any mechanic from the design draft while fixing a flaw.
- Every component you reference MUST be an exact component_id from the pool, and quantities must not 
  exceed the pool. Do not introduce pieces that are not in the pool.
- If a fix changes a rule, update every other section that mentions that rule (setup, scoring, FAQ) so 
  the rulebook stays consistent.
- Keep the flowing-prose style; numbered steps only for strictly sequential procedures.

Respond with ONLY a JSON object matching the provided schema. No other text. Represent line breaks as '\\n'
 in JSON string values.
"""

    flaw_block = "\n".join(
        f"{i}. [{flaw.flaw_type}] section: {flaw.section} | affects: {flaw.affected_target}\n"
        f"  Evidence: \"{flaw.evidence_quote}\"\n"
        f"  Problem: \"{flaw.problem}\"\n"
        f"  Consequence: \"{flaw.mda_chain}\"\n"
        f"  Suggested direction: \"{flaw.repair_suggestions}\"\n"
        for i, flaw in enumerate(flaws, 1)
    )

    user_prompt = f"""## Design Draft
{draft_block(draft, mechanics_by_id)}

## Component Pool (select component_id from these ONLY)
{format_pool_block(pool)}

## Current Rulebook (JSON)
{current.model_dump_json(indent=2)}

## Flaws to Fix
{flaw_block}

## Task
Return the full corrected rulebook as ONE JSON object matching this schema:
{schema_json(NewGame)}
"""

    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]

FLAW_TAXONOMY = """M - Mechanics layer (the actual written rules)
- M_critical: A structural or gatekeeping rule is missing or broken so the game
  cannot be played or finished as written. E.g no win condition or end trigger, no
  way to resolve a core action, a phase transition that is never explained.
- M_major: A rule exists, but a limit, cost, penalty or prerequisite inside it is wrong or
  absent, which distorts play without stopping it. E.g no hand limit where one is needed,
  a powerful action with no cost.
- M_minor: A small numeric slip, ambiguous wording or an edge case that two sections handle 
  inconsistently.

D - Dynamics layer (what emerges when the rules are actually played)
- D_critical: Each mechanic works alone but nothing connects them; the output of one never 
  feeds another, so the game is a pile of disconnected subsystems.
- D_major: The game still runs, but its central tension or feedback loop is gone. E.g. a
  dominant strategy, decisions that do not matter, no reason for players to interact.
- D_minor: A timing or sequencing detail sits in the wrong place between phases. E.g. trading
  is only allowed after players have already committed their cards.

A - Aesthetics layer (the intended experience)
- A_major: The rules produce an experience that contradicts the draft's target experience or
  core tension.
- A_minor: The theme is only cosmetic, generic or inconsistent. E.g. flavour that does not match
  the setting, or two settings awkwardly mixed.
"""

def draft_block(draft: DesignDraft, mechanics_by_id: dict[str, Mechanic]) -> str:
    def tier(entries: Sequence[CoreMechanic | SupportingMechanic | StructuralMechanic]) -> str:
        tier = []
        for entry in entries:
            mechanic: Mechanic = mechanics_by_id.get(entry.mechanic_id)
            if mechanic:
                tier.append(
                    f" - {mechanic.name}: {mechanic.description}"
                    f"   Role in this design: {entry.rationale}"
                ) 
        return "\n".join(tier) or "(none)"
    
    design, params = draft.design_intent, draft.parameters
    return f"""Pitch: {draft.concept.elevator_pitch}
Target experience: {design.target_experience}
Core tension: {design.core_tension}
Theme-mechanic fit: {design.theme_mechanic_fit}
Core mechanics:
{tier(draft.mechanics.core)}
Supporting mechanics:
{tier(draft.mechanics.supporting)}
Structural mechanics:
{tier(draft.mechanics.structural)}
Intended parameters: complexity {params.complexity}/5, players {params.player_count[0]}-{params.player_count[1]}
""" 

def new_game_block(game: NewGame, pool: ComponentPool) -> str:
    def comp_entry_str(component: ComponentEntry) -> str:
        comp = pool.get_component_by_id(component.component_id)
        label = comp.name if comp else component.component_id
        note = f" ({component.notes})" if component.notes else ""
        return f"- {component.quantity}x {label}{note}"
    
    components = "\n".join(comp_entry_str(comp_entry) for comp_entry in game.components)
    faqs = "\n".join(f"Question: {faq.question}\nAnswer: {faq.answer}" for faq in game.faq) or "(none)"

    return f"""### lore_and_objective
{game.lore_and_objective}

### components
{components}

### setup
{game.setup}

### gameplay_flow
{game.gameplay_flow}

### core_mechanics
{game.core_mechanics}

### scoring_and_endgame
{game.scoring_and_endgame}

### faq
{faqs}
"""

def game_critic_new_game_diagnose(
    draft: DesignDraft,
    game: NewGame,
    pool: ComponentPool,
    mechanics_by_id: dict[str, Mechanic]
) -> list[dict]:
    system_prompt = f"""You are a meticulous board game rulebook reviewer. You are given a design 
draft (what the rulebook was meant to implement) and the rulebook written from it. Find genuine 
design flaws in the rulebook -- problems that would break, distort or undermine play -- and report
each one precisely. You are reviewing the rulebook, not rewriting it; a repair suggestion is a short
direction for the fix, not replacement text.

Review the rulebook in this order:
1. Mechanics fidelity: is every core and supporting mechanic from the draft implemented as usable rules? 
   Is anything essential missing: a win condition, a game-end trigger, the turn structure or how a mechanic
   resolves?
2. Consistency: do any rules contradict each other, or contradict the setup, scoring or FAQ? Do the rules
   use pieces that not in the component list.
3. Dynamics: read the turn structure as if you were playing it. Does each phase have what it needs from the 
   phases before it? Do the mechanics feed each other? Is there still a real decison, or is there a dominant
   strategy or a phase where nothing matters?
4. Intent and theme: do the rules actually produce the target experience and core tension in the draft? Does 
   theme show up in the rules, or is it generic or mismatched?

Flaw types (choose exactly one per flaw):
{FLAW_TAXONOMY}

Reporting rules:
- Report at most 3 flaws, order by severity (most severe first). Report fewer if fewer exist.
- Every flaw needs verbatim evidence: the section name plus a short passage copied exactly from
  that section. For a missing rule, quote the sentence where the gap becomes apparent.
- Before reporting something as missing, check the other sections and the FAQ; it may be handled 
  there.
- Do NOT report: writing style or tone, a wish for more detail or flavour, or component quantities 
  (those are verified separately in code before handing to you).
- Do not invent problems. A rulebook with no real flaws is a valid result: return an empty 
  flaws list. Never flag something just to have something to report.
- Judge only what the text says. Do not assume rules from real games.

Respond with ONLY a JSON object matching the provided schema. No other text.
"""

    user_prompt = f"""## Design Draft
{draft_block(draft, mechanics_by_id)}

## New game
{new_game_block(game, pool)}

## Task
Review the rulebook above and report its genuine flaws. Produce a single JSON object matching
this schema:
{schema_json(Diagnosis)}

Reminder: quote evidence exactly, report at most 3 flaws, and return an empty list if there is
nothing worth fixing.
"""    

    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

def game_critic_new_game_compare(
    draft: DesignDraft,
    game_1: NewGame,
    game_2: NewGame,
    pool: ComponentPool,
    mechanics_by_id: dict[str, Mechanic]
) -> list[dict]:
    system_prompt = f"""You are a meticulous board game rulebook reviewer. You are given a 
design draft and two versions of a rulebook written from it. Decide which version has fewer and
less severe genuine design flaws.

A flaw is a problem that would break, distort or undermine play. These are the kinds to look for:
{FLAW_TAXONOMY}

Procedure:
1. List up to 3 genuine flaws in version 1, order by severity (most severe first), one short line each.
   Leave the list empty if there are none.
2. Do the same for version 2.
3. Weigh the two lists by severity: one critical flaw outweighs several minor ones.
4. Set "preferred" to the version with the better set of flaws or "tie" if they are equivalent or you can't tell.

Rules:
- The order which the versions are provided carries no meaning.
- Length, polish and extra details are not merits. Judge design quality only.
- A version that drops or ignores a mechanic from the draft has a flaw.
- Do not invent problems. If a version has no real flaws, its list is empty.

Respond with ONLY a JSON object matching the provided schema. No other text.
"""
    
    user_prompt = f"""## Design Draft
{draft_block(draft, mechanics_by_id)}

## Version 1
{new_game_block(game_1, pool)}

## Version 2
{new_game_block(game_2, pool)}

## Task
Compare the two versions. Produce a single JSON object matching this schema:
{schema_json(Comparison)}
"""

    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

# scale game
def game_scaler(
    title: str, 
    og_rules: str, 
    pool: ComponentPool, 
    scaling_brief: str
) -> list[dict]:
    system_prompt = """You are a board game developer adapting an EXISTING game to a new player count
and/or difficulty. You are not designing a new game: keep the original's theme, core loop and identity 
intact and change as little as possible.

You are shown EXCERPTS of the original rules, not the whole rulebook. Do not assume rules that are not
shown. If the excerpts are not enough to modify a rule safely, leave that rule rather than guessing.

Rules:
- Describe your work as a list of changes to the original rules. Each change must quote the original passage
  it modifies, copied verbatim, unless it adds a new rule.
- Only use the components available in the pool and never more than the available quantity. If the target needs
  more pieces than the pool holds, adapt the rules instead (e.g. teams, shared pieces, smaller per-player allocations) 
  rather than exceeding the pool.
- Player scaling levers: teams or partnerships, shared pieces, per-player allocations, board size or layout, simultaneous 
  play to cut downtime, a dummy/automated opponent for low counts.
- Difficulty levers. Easier: more starting resources, softer penalties, fewer options per turn, removing a subsystem. Harder: 
  tigher resources, added obstacles, removing safety nets, hidden information.
- Follow the scope limi in the brief. Do not make changes the did not ask for.
- The variant's new_player_count must cover the whole target range in the brief.

Respond with ONLY a JSON object matching the provided schema. No other text. Represent line breaks as '\\n' in
JSON string values.

"""

    user_prompt = f"""## Original game
{title}

## Original rules (excerpts)
{og_rules}

## Component Pool (select ONLY from these by component_id)
{format_pool_block(pool)}

## Scaling brief
{scaling_brief}

## Task
Produce the scaled variant as a single JSON object matching this schema:
{schema_json(ScaledGame)}
"""

    return [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]