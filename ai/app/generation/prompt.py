import logging
import json
from typing import Optional
from app.schemas.ggaia_schemas import (
    DesignDraft, 
    NewGame, 
    ComponentPool,
    Mechanic
)

logger = logging.getLogger(__name__)


def build_chat_messages(query: str, retrieved_chunks: list[dict]) -> list[dict]:
    """
    Constructs the message payload for the LLM chat completion API.
    Enforces a strict adjudicator persona using XML boundaries to prevent
    hallucinations in smaller fallback models.
    """

    context_texts = []
    for i, chunk in enumerate(retrieved_chunks, start=1):
        context_texts.append(f"[Excerpt{i}]\n{chunk.get('content', '')}")

    context_block = "\n\n".join(context_texts)

    system_prompt = (
        "You are an expert tabletop board game rules adjudicator. "
        "Your sole purpose is to answer user question using ONLY the text provided inside the <context> tags. "
        "Adhere strictly to these rules:\n"
        "1. Do not use outside knowledge, assume rules, or hallucinate mechanics.\n"
        "2. If the <context> does not explicitly contain the answer, you must reply exactly with: "
        "'I cannot find the answer to this rule in the provided text.'\n"
        "3. Be concise, direct, and clear in your explanation."
    )

    user_prompt = (
        f"<context>\n"
        f"{context_block}\n"
        f"</context>\n\n"
        f"<question>\n"
        f"{query}\n"
        f"</question>\n\n"
        f"Answer the <question> based strictly on the <context> above."
    )

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

    return messages

#-------- build ggaia generation prompts ---------

# new game 
def game_ideator_new_game(
    parent_a: dict,
    parent_b: dict,
    potential_mechanics: list[dict],
    available_component_types: set[str]
) -> list[dict]:
    system_prompt = """You are a board game design analyst. You will be given two
"parent" games and a pool of physical components available to build with. Your job
is to design a SINGLE new game that meaningfully combines ideas from both parents, using
only mechanics that are physically feasible given the component pool.

Rules:
- Every mechanic you select (core, supporting or structural) MUST come from the provided
  candidate mechanics list. Reference mechanics by their mechanic_id.
- Before selecting a mechanic, check that every component type it requires is present in the
  available component types. Never select an infeasible mechanic.
- The two parents may have different, overlapping or no classified types at all -- pick 
  whichever classification type best fits the NEW design on its own merits.
- Synthesize a coherent theme for the new design; you are not required to reuse either parent's theme verbatim,
  but the result should feel intentional, not like two settings frankensteined together.
- Every mechanic's rationale must explain its role in THIS new design.

Respond with ONLY a JSON object matching the provided schema. No other text.
"""

    def generate_parent_string(parent: dict):
        parent_types = ", ".join(parent['types']) if parent['types'] else "No classified type"
        parent_genres = ", ".join(parent['genres'])
        return (
            f"{parent['title']} ({parent_types}; genres: {parent_genres})\n"
            f" {parent['description']}"
        )

    mechanic_block = "\n".join(
        f"- id={m['mechanic_id']} | {m['name']} ({m['category']}): {m['description']}"
        f"[requires: {', '.join[m['requires_component_types']]}]"
        for m in potential_mechanics
    )

    available_components_str = ", ".join(sorted(available_component_types))

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
{json.dumps(DesignDraft.model_json_schema(), indent=2)}
"""
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

    return messages

def game_realizer_new_game(
    draft: dict, 
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

Respond with ONLY a JSON object matching the provided schema. No other text. Represent line breaks as '\n' in
JSON string values.
"""
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

    chosen_mechanics = (
        [m['mechanic_id'] for m in draft['mechanics']['core']] +
        [m['mechanic_id'] for m in draft['mechanics']['supporting']] +
        [m['mechanic_id'] for m in draft['mechanics']['structural']]
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
{json.dumps(draft, indent=2)}

## Mechanics used in the draft
{mechanic_block}

## Component Pool (select ONLY from these, by component_id)
{format_pool_block(pool)}

## Task
Write the complete rulebook for the design draft above. Produce a single JSON object
matching this schema:
{json.dumps(NewGame.model_json_schema(), indent=2)}
"""
    
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

    return messages

def game_critic_new_game(
    game_to_critic: NewGame
) -> None:
    system_prompt = None
    user_prompt = None


    
    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt},
    ]

    return messages

# scale game