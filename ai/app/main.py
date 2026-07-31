import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import rulebook
from app.services import mongo_service
from app.services import r2_service

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Executes startup and shutdown logic
    """
    # Startup
    logger.info("Starting Boardwise AI Gateway...")
    try:
        # Ping mongo
        mongo_service.ping_database()
        logger.info("MongoDB connection verified.")

        # Ping R2
        r2_service.ping_r2_storage()
        logger.info("Connection to R2 bucket verified")
    except Exception as e:
        logger.exception("FATAL BOOT ERROR: Infrastructure check failed")
        raise e

    yield

    # Shutdown
    logger.info("Shutting down AI Gateway...")
    mongo_service.client.close()

app = FastAPI(
    title="Boardwise AI Gateway",
    description="Asynchronous ingestion pipeline for rulebook processing.",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8080", "http://localhost:3000", "https://www.boardwise.games", "https://boardwise.games"],
    # allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

app.include_router(rulebook.router)

@app.get("/health", tags=["System"])
async def health_check():
    """A lightweight endpoint for health checks"""
    return{"status": "healthy", "service": "ai-gateway"}