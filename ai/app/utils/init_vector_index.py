import logging

from app.config import settings
from pymongo import MongoClient
from pymongo.errors import OperationFailure
from pymongo.operations import SearchIndexModel

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)


def initialise_vector_index():
    """
    Initialises the vector index for embeddings
    """
    client = MongoClient(settings.MONGODB_URL)

    if settings.MONGODB_DATABASE is None:
        raise ValueError("DB_NAME is not set in the environment")
    db = client[settings.MONGODB_DATABASE]
    collection = db["RULEBOOK_TEXT"]

    index_definition = {
        "fields": [
            {
                "type": "vector",
                "path": "embedding",
                "numDimensions": 256,  # The Matryoshka dimension size determined for the Nomic model (must match exactly)
                "similarity": "cosine",
                "quantization": "binary"
            },
            {
                "type": "filter",  # MongoDB will shrink search space to chunks of a specific rulebook before performing similarity calculations
                "path": "rulebookId",
            },
        ]
    }
    
    try:
        logger.info("Attempting to drop existing vector index...")
        collection.drop_search_index("vector_index")
    except OperationFailure:
        logger.info("No existing index to drop.")

    search_index_model = SearchIndexModel(
        definition=index_definition, name="vector_index", type="vectorSearch"
    )

    try:
        logger.info("Creating vector index")
        if "RULEBOOK_TEXT" not in db.list_collection_names():
            db.create_collection("RULEBOOK_TEXT")
        collection.create_search_index(model=search_index_model)
        logger.info("Vector index 'vector_index' created successfully.")
    except OperationFailure:
        logger.exception("Index creation failed (Index may already exist)")

if __name__ == "__main__":
    initialise_vector_index()
