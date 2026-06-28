from bson import ObjectId
import logging

logger = logging.getLogger(__name__)

def generate_chunks(full_text: str) -> tuple[bool, list[dict], str]:
    """
    Splits the extracted text into logical chunks for MongoDB and Vectorisation.
    Returns: (success, list_of_chunks, failure_reason)
    """
    try:
        # 1. Semantic Splitting
        raw_chunks = full_text.split("\n\n")

        chunks = []
        current_index = 0

        # 2. Schema Mapping
        for chunk in raw_chunks:
            clean_chunk = chunk.strip()

            if not clean_chunk:
                continue

            new_chunk = {
                "chunkId": str(ObjectId()),
                "index": current_index,
                "content": clean_chunk
            }
            chunks.append(new_chunk)
            current_index += 1

        # 3. Validation
        if not chunks:
            return (False, [], "Chunks resulted in 0 valid segments")

        logger.info(f"Successfully generated {len(chunks)} chunks.")
        return(True, chunks, "")
    except Exception as e:
        logger.error(f"Chunking failed: {str(e)}", exc_info=True)
        return (False, [], "Internal error occured during text chunking")
    