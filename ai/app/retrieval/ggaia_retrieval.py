from typing import Optional
from bson import ObjectId
from random import randint

from app.services.mongo_service import get_db
from app.services.ggaia_new_game import GenerationInput
from app.services.ggaia_game_scaling import ScaleInput
from app.retrieval.similar_mechanics import (
    get_mechanics_for_games,
    get_components_for_games
)
from app.schemas.ggaia_schemas import (
    Mechanic,
    ComponentPool
)


def load_new_inputs(
    games_for_inspo: list[str] | str,
    user_id: Optional[str] = None
) -> GenerationInput:
    db = get_db()
    boardgames = db.get_collection("BOARD_GAME")
    user = db.get_collection("USER").find_one({"_id": ObjectId(user_id)})
    if isinstance(games_for_inspo, str):
        user_game_ids = user['ownedGames']
        user_game_count = len(user_game_ids)
        if user_game_count == 2:
            games_for_inspo = user_game_ids
        elif user_game_count > 2:
            rand_indexes = [randint(0, user_game_count - 1) for _ in range(2)]
            games_for_inspo = [user_game_ids[i] for i in rand_indexes]
        else: 
            raise ValueError("Not enough games in inventory to be used for \"Surprise Me\" or generation in general.")
    
    actualgames = []
    for id in games_for_inspo:
        game = boardgames.find_one({"_id": ObjectId(id)})
        actualgames.append(game)

    mech_dicts = get_mechanics_for_games(games_for_inspo)
    components = get_components_for_games(games_for_inspo)

    pool = ComponentPool(**components)
    mechanics: list[Mechanic] = []
    for mech in mech_dicts:
        mechanic = Mechanic(
            mechanic_id=mech["mechanic_id"],
            name=mech["name"],
            category=mech["category"],
            description=mech["description"],
            requires_component_types=mech["require_component_types"]
        )
        mechanics.append(mechanic)

    ## -- Potentially add vector search to test generation --

    return GenerationInput(
        actualgames[0],
        actualgames[1],
        pool,
        mechanics
    )

def load_scale_inputs(
    game_to_scale: str
) -> ScaleInput:
    db = get_db()
    game_id_object_id = ObjectId(game_to_scale)
    boardgame = db.get_collection("BOARD_GAME").find_one("_id", game_id_object_id)
    rulebook = db.get_collection("RULEBOOK").find_one({"gameId": game_id_object_id})
    components = get_components_for_games([game_to_scale])
    pool = ComponentPool(**components)

    return ScaleInput(
        title=boardgame["title"],
        rulebook_id=rulebook["_id"],
        stats=boardgame["stats"],
        pool=pool
    )
    
