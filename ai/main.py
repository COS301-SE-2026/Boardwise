from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import rulebook

app = FastAPI(
    title="Boardwise AI Gateway Skeleton",
    description="Rebuild of the FastAPI ingestion pipeline"
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
    return{"status": "ok", "message": "Gateway is breathing."}