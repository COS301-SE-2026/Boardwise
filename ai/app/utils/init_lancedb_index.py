import argparse
import logging

from app.services import lancedb_service

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

def initialise_lancedb(force: bool = False) -> None:
    lancedb_service.get_table()
    
    if lancedb_service.is_index_ready() and not force:
        logger.info("LanceDB indexes have already been initialised.")
        return
    
    lancedb_service.ensure_indexes(force_recreate=force)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--force",
        action="stored_true",
        help="Rebuild indexes even if they already exist"
    )
    args = parser.parse_args()
    initialise_lancedb(force=args.force)