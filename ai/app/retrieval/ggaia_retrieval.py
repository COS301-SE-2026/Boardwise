from typing import Optional

from app.services.ggaia_new_game import GenerationInput
from app.services.ggaia_game_scaling import ScaleInput


def load_new_inputs(
    games_for_inspo: list[str] | str,
    user_id: Optional[str] = None
) -> GenerationInput:
    """ 
        Function not yet implement at this point. Will be implemented 
        during the course of development. Present in PR as it was predefined
    """
    pass

def load_scale_inputs(
    game_to_scale: str
) -> ScaleInput:
    pass