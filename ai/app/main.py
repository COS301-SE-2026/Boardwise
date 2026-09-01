import logging
from contextlib import asynccontextmanager

import torch
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from llama_cpp import Llama
from sentence_transformers import CrossEncoder, SentenceTransformer

from app.config import settings
from app.routers import internal, job, rulebook
from app.services import mongo_service, r2_service
from app.utils.init_vector_index import initialise_vector_index

logging.basicConfig(
    level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger(__name__)

torch.set_num_threads(settings.CPU_CORES)
torch.set_num_interop_threads(settings.CPU_CORES)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Executes startup and shutdown logic
    """
    logger.info("Booting Boardwise AI Gateway. Loading models into memory...")

    ml_models = {}

    try:
        mongo_service.ping_database()
        logger.info("MongoDB connection verified.")

        r2_service.ping_r2_storage()
        logger.info("Connection to R2 bucket verified.")

        initialise_vector_index()

        # device="cpu" is set to avoid searching for CUDA on Fargate
        # trust_remote_code=True is required for Nomic models via HuggingFace
        ml_models["embedding_model"] = SentenceTransformer(
            "nomic-ai/nomic-embed-text-v1.5",
            device="cpu",
            revision="e9b6763023c676ca8431644204f50c2b100d9aab",
            trust_remote_code=True,
        )
        logger.info("Nomic embedding model loaded successfully.")

        ml_models["reranker_model"] = CrossEncoder(
            "cross-encoder/ms-marco-MiniLM-L-6-v2", device="cpu"
        )
        logger.info("Cross-encoder re-ranker model loaded successfully.")

        ml_models["local_llm"] = Llama(
            model_path="/app/models/qwen2.5-0.5b-instruct-q4_k_m.gguf",
            n_ctx=4096,
            n_threads=settings.CPU_CORES,
            n_gpu_layers=0,
            verbose=False,
        )
        logger.info("Local LLM model loaded successfully.")

        app.state.ml_models = ml_models
    except Exception:
        logger.exception("FATAL BOOT ERROR: Infrastructure check failed")
        raise

    yield

    logger.info("Shutting down AI Gateway. Clearing memory footprint...")
    mongo_service.client.close()
    del ml_models["local_llm"]
    ml_models.clear()


app = FastAPI(
    title="Boardwise AI Gateway",
    description="""
    An asynchronous Retrieval-Augmented Generation (RAG) gateway for the Boardwise tabletop gaming platform.
    
    Core Capabilities:
    - Ingestion Pipeline: PDF sanitisation, OCR text extraction, and hierarchical chunking.
    - Vectorisation: Matryoshka dimensionality truncation and MongoDB Binary Quantization via Nomic embeddings.
    - Retrieval & Generation: Vector similarity search, cross-encoder re-ranking, and LLM context generation.
    """,
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8080",
        "http://localhost:3000",
        "https://www.boardwise.games",
        "https://boardwise.games",
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(rulebook.router, prefix="/api/vault/rulebooks")
app.include_router(job.router, prefix="/api/vault/jobs")
app.include_router(internal.router, prefix="/api/vault/internal")


@app.get("/health", tags=["System"])
async def health_check():
    """A lightweight endpoint for health checks"""
    return {"status": "healthy", "service": "ai-gateway"}
