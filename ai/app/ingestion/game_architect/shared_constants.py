_COMPONENT_EXTRACTION_SCHEMA = {
    "type": "object",
    "properties": {
        "components": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "type": {
                        "type": "string",
                        "enum": ["board", "card", "die", "token", "meeple", "tile", "miniature", "other"]
                    },
                    "name": {"type": "string"},
                    "quantity": {"type": "integer"},
                    "attributes": {"type": "object"},
                },
                "required": ["type", "name", "quantity"]
            }
        }
    },
    "required": ["components"]
}