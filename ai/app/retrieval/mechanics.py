from app.schemas.ggaia_schemas import Component, Mechanic


def get_similar_mechanics(
    user_inventory: list[Component], top_k: int = 5
) -> list[Mechanic]:
    """
    Returns the k most relevant mechanics to use as grounding context
    for game generation when given a user's owned boardgame components.
    """
    return [
        Mechanic(
            mechanic_id="mech-001",
            name="Worker Placement",
            category="Strategy",
            description="Players place tokens on action spaces to trigger effects.",
            requires_component_types=["token", "board"],
            min_players=2,
            max_players=4,
        ),
        Mechanic(
            mechanic_id="mech-002",
            name="Deck Building",
            category="Card Game",
            description="Players start with a small deck and draft new cards to improve it over time.",
            requires_component_types=["card"],
            min_players=1,
            max_players=4,
        ),
    ][:top_k]
