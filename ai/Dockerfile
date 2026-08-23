FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Set the working directory inside the container
WORKDIR /app

# Install system-level dependencies
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-eng \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create a directory for local models and download Qwen3-0.6B GGUF
RUN mkdir -p /app/models && \
    curl -L -o /app/models/qwen2.5-0.5b-instruct-q4_k_m.gguf \
    "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf"

# Copy requirements file and install Python dependencies
COPY requirements.txt .
RUN pip install --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt --extra-index-url https://abetlen.github.io/llama-cpp-python/whl/cpu

# Copy the rest of the application code
COPY . .

# Expose the port Uvicorn runs on
EXPOSE 8000

# Command to run the application and making it accessible outside the container
CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]