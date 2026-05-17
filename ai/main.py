from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.routers import rulebook

app = FastAPI(
    title="Boardwise AI Gateway",
    description="FastAPI ingestion pipeline for The Vault",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)

app.include_router(rulebook.router)


@app.get("/health")
def health_check():
    return {"status": "ok"}