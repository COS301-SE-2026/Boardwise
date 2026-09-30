import logging
from collections import defaultdict

from bson import ObjectId
from bson.errors import InvalidId

from app.services import mongo_service

logger = logging.getLogger(__name__)


def get_mechanics_for_games(
    game_ids: list[str],
    user_inventory: list[dict] | None = None,
    max_mechanics: int | None = None,
) -> list[dict]:
    """Return usable mechanics for the given games"""
    object_ids = _parse_object_ids(game_ids)
    if not object_ids:
        logger.warning("get_mechanics_for_games called with no valid game ids")
        return []

    games = mongo_service.get_boardgames_by_ids(object_ids)
    missing_games = set(object_ids) - {g["_id"] for g in games}
    if missing_games:
        logger.warning(
            "BOARD_GAME docs not found for ids: %s", sorted(map(str, missing_games))
        )

    if not games:
        return []

    bgg_ids = {b for g in games for b in _get_bgg_ids_from_game(g)}
    if not bgg_ids:
        logger.warning(
            "None of the games have mechanics recorded: %s",
            [g.get("title", str(g["_id"])) for g in games],
        )
        return []

    mechanic_docs = mongo_service.get_mechanics_by_bgg_ids(bgg_ids)

    owned_types = None
    if user_inventory is not None:
        type_totals: dict[str, int] = defaultdict(int)
        for c in user_inventory:
            t = c.get("type")
            if not t:
                continue
            q = c.get("quantity")
            if isinstance(q, (int, float)) and q > 0:
                type_totals[t] += q
        owned_types = {t for t, total in type_totals.items() if total > 0}
        if not owned_types:
            logger.warning("Inventory has no component types with positive quantity")

    usable = select_usable_mechanics(games, mechanic_docs, owned_types)
    usable.sort(key=lambda u: (-len(u["source_game_ids"]), u["doc"]["name"]))
    if max_mechanics is not None:
        usable = usable[:max_mechanics]

    return [_to_mechanic_shape(u) for u in usable]


def get_components_for_games(game_ids: list[str]) -> dict:
    object_ids = _parse_object_ids(game_ids)
    if not object_ids:
        logger.warning("get_components_for_games called with no valid game ids")
        return {"source_game_ids": [], "components": []}

    rulebook_by_game = mongo_service.get_latest_rulebook_per_game(object_ids)

    inventory: list[dict] = []
    for game_id in object_ids:
        rulebook_id = rulebook_by_game.get(str(game_id))
        if rulebook_id is None:
            logger.warning("No Ready rulebook for game %s", game_id)
            continue

        raw_components = mongo_service.get_components_for_rulebook(rulebook_id)
        for raw in raw_components:
            shaped = _to_component_shape(raw)
            if shaped is not None:
                inventory.append(shaped)

    logger.info(
        "Built inventory of %d components from %d/%d games.",
        len(inventory),
        len(rulebook_by_game),
        len(object_ids),
    )
    return {"source_game_ids": game_ids, "components": inventory}


def select_usable_mechanics(
    games: list[dict], mechanic_docs: list[dict], owned_types: set[str] | None
) -> list[dict]:
    docs_by_bgg_id = {
        d["bggId"]: d for d in mechanic_docs if d.get("bggId") is not None
    }
    sources: dict[int, list[str]] = defaultdict(list)
    for g in games:
        for bgg_id in set(_get_bgg_ids_from_game(g)):
            sources[bgg_id].append(str(g["_id"]))

    usable: list[dict] = []
    unknown: list[int] = []
    unauthored: list[str] = []
    infeasible: list[str] = []

    for bgg_id, game_list in sources.items():
        doc = docs_by_bgg_id.get(bgg_id)
        if doc is None:
            unknown.append(bgg_id)
            continue
        if not doc.get("description"):
            unauthored.append(doc.get("name", str(bgg_id)))
            continue
        required = set(doc.get("requiresComponentTypes") or [])
        if owned_types is not None and not required <= owned_types:
            infeasible.append(doc["name"])
            continue
        usable.append({"doc": doc, "source_game_ids": sorted(game_list)})

    if unknown:
        logger.warning(
            "Mechanics have game reference but are missing from MECHANIC (bggIds): %s",
            sorted(unknown),
        )
    if unauthored:
        logger.warning(
            "Mechanics skipped, no description authored: %s", sorted(unauthored)
        )
    if infeasible:
        logger.info(
            "Mechanics dropped as infeasible for the inventory: %s", sorted(infeasible)
        )
    return usable


def _get_bgg_ids_from_game(game: dict) -> list[int]:
    ids: list[int] = []
    for entry in game.get("mechanics") or []:
        if isinstance(entry, dict):
            if entry.get("bggId") is not None:
                ids.append(entry["bggId"])
        elif isinstance(entry, int):
            ids.append(entry)
    return ids


def _parse_object_ids(game_ids: list[str]) -> list[ObjectId]:
    parsed: list[ObjectId] = []
    for gid in game_ids or []:
        try:
            parsed.append(ObjectId(str(gid)))
        except (InvalidId, TypeError):
            logger.warning("Ignoring invalid game id: %r", gid)
    return parsed


def _to_mechanic_shape(usable: dict) -> dict:
    m = usable["doc"]
    return {
        "mechanic_id": m["_id"],
        "name": m["name"],
        "category": m.get("category", ""),
        "description": m["description"],
        "requires_component_types": m.get("requiresComponentTypes", []),
        "bgg_id": m["bggId"],
        "source_game_ids": usable["source_game_ids"],
    }


def _to_component_shape(raw: dict) -> dict | None:
    component_id = raw.get("componentId")
    if not component_id:
        return None

    quantity = raw.get("quantity")
    if not isinstance(quantity, int) or isinstance(quantity, bool) or quantity < 1:
        return None

    return {
        "component_id": component_id,
        "type": raw.get("type", "other"),
        "name": raw.get("name", ""),
        "quantity": quantity,
        "attributes": raw.get("attributes") or {},
    }
