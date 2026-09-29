FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV HOME=/app
ENV HF_HOME=/app/cache
ENV SENTENCE_TRANSFORMERS_HOME=/app/cache

ARG EMBED_MODEL_REVISION=e9b6763023c676ca8431644204f50c2b100d9aab

RUN groupadd -g 1001 fast && useradd -u 1001 -g fast -m -d /app -s /bin/false fast

# Set the working directory inside the container
WORKDIR /app

# Install system-level dependencies, create a directory for local models and the model cache.
# Only the cache is writable by the app user; code and models stay root-owned.
RUN apt-get update && apt-get install -y --no-install-recommends \
    gosu \
    tesseract-ocr \
    tesseract-ocr-eng \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/models /app/cache \
    && chown -R fast:fast /app/cache

# Root-owned, world-readable (remote ADD defaults to 600, so set the mode explicitly)
ADD --chmod=644 \
    "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf" \
    /app/models/qwen2.5-3b-instruct-q4_k_m.gguf

# Copy requirements file and install Python dependencies
COPY requirements.txt .

RUN pip install --upgrade --no-cache-dir --only-binary :all: pip==26.2 \
    && pip install --no-cache-dir --only-binary :all: --require-hashes -r requirements.txt \
    && python -c "from sentence_transformers import SentenceTransformer; \
    SentenceTransformer('nomic-ai/nomic-embed-text-v1.5', \
    revision='${EMBED_MODEL_REVISION}', trust_remote_code=True)" \
    && python -c "from sentence_transformers import CrossEncoder; \
    CrossEncoder('cross-encoder/ms-marco-MiniLM-L-6-v2')" \
    && chown -R fast:fast /app/cache

# Copy the rest of the application code (root-owned, not modifiable by the app user)
COPY app/ ./app/
COPY scripts/ ./scripts/
COPY --chmod=755 entrypoint.sh /usr/local/bin/entrypoint.sh


# Expose the port Uvicorn runs on
EXPOSE 8000

# Command to run the application and making it accessible outside the container
CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]