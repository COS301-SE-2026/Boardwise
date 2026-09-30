import logging

from app.ingestion.game_architect.mechanics_data import SEED_MECHANICS
from app.services import mongo_service

logger = logging.getLogger(__name__)


def seed_mechanics() -> tuple[bool, str]:
    """Upserts all seed mechanics into Mongo. Returns (success, reason)"""
    try:
        mongo_service.upsert_mechanics(SEED_MECHANICS)
        logger.info("Seeded %d mechanics into MECHANIC collection", len(SEED_MECHANICS))
        return (True, "")
    except Exception:
        logger.exception("Failed to seed mechanics.")
        return (False, "Mechanics seeding failed")
