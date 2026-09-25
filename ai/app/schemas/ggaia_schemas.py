from typing import Any, Literal

from pydantic import BaseModel

ComponentType = Literal[
    "board", "card", "die", "token", "meeple", "tile", "miniature", "other"
]


class Component(BaseModel):
    component_id: str
    type: ComponentType
    name: str
    quantity: int
    attributes: dict[str, Any]

class Mechanic(BaseModel):
    mechanic_id: str
    name: str
    category: str
    description: str
    requires_component_types: list[ComponentType]
    min_players: int
    max_players: int