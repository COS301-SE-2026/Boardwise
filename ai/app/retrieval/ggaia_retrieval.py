from typing import Optional

from ai.app.services.ggaia_new_game import GenerationInput
from app.services.mongo_service import get_db


def load_inputs(
    games_for_inspo: list[str] | str,
    user_id: Optional[str] = None
) -> GenerationInput:
    """ 
        Function not yet implement at this point. Will be implemented 
        during the course of development. Present in PR as it was predefined
    """
    pass