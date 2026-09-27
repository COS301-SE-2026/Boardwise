import logging
import json
from typing import Optional
from app.schemas.ggaia_schemas import DesignDraft, NewGame, ComponentPool

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
    parent_a_rulebook_exp: Optional[str] = None,
    parent_b_rulebook_exp: Optional[str] = None,
) -> list[dict]:

    system_prompt = None
    user_prompt = None
    
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