# Setup Wizard Prompt
import logging
from typing import Literal
from app.schemas.setup_wizard_schemas import LLMStep

logger = logging.getLogger(__name__)


PhaseKey = Literal["board", "components", "players", "first_player"]
PHASE_LABELS: [PhaseKey, str] = {
    "board": "Board Setup",
    "components": "Shared Components",
    "players": "Player Setup",
    "first_player": "First Player",
}

PHASE_ORDER: list[PhaseKey] = ["board", "components", "players", "first_player"]

PHASE_DESCRIPTIONS ={
    "board": "laying out the board, map, or shared play area",
    "components": "shared piles, decks, tokens, or dice placed once for the whole game, not owned by any one player",
    "players": "what EACH player individually takes, places, or does, including setup differences by player count",
    "first_player": "ONLY choosing who goes first and very last check before play begins",
}

def build_phase_messages(phase: PhaseKey, chunks: list[dict]) -> list[dict]:
    context = "\n\n".join(f"[Chunk {c['index']}]\n{c['content']}" for c in chunks)

    system = (
        "You extract board game setup steps from a rulebook, for ONE category only: "
        f"'{phase}' ({PHASE_DESCRIPTIONS[phase]}).\n"
        "Use ONLY the text inside the <context> tags. Rules:\n"
        "1. Only list steps that belong to this one category. If nothing in the context "
        "belongs here, return an empty steps list - do not invent anything.\n"
        "2. Both 'title' (a short label, under 10 words) and 'instruction' (the full "
        "one or two sentence description) are REQUIRED and must never be empty.\n"
        "3. Every step must cite the chunk numbers it came from in sourceChunks, using "
        "ONLY chunk numbers that appear in the context.\n"
        "4. Copy quantities as exactly as written. NEVER invent a quantity not in the context.\n"
        "5. Every component involved must also appear in that step's own 'components' list, "
        "with the name and quantity exactly as written in the cited chunk. \n"
        "6. scope: 'shared' = done once for the whole table. 'per_player' = each player "
        "does their own copy of this action.\n"
        "7. Never list the same step twice."
        "Example of a correctly filled step:\n"
        '{"title": "Dealing Starting Cards", "instruction": "Each player receives five card s"'
        'from the shuffled deck.", "components[{"name": "cards", "quantity":"five"}], '
        '"scope":"per_player", "sourceChunks": [4]'
    )

    user =(
        f"<context>\n{context}\n<context>\n\n"
        f"<task>\nList Every '{phase}' setup step found in the context above.\n<task>"
    )

    return [
        {"role": "system", "content": system},
        {"role": "user", "content": user},
    ]