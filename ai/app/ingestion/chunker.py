import logging

from bson import ObjectId

logger = logging.getLogger(__name__)


def generate_chunks(
    blocks_cache: dict, max_chunk_size: int = 1000
) -> tuple[bool, list[dict], str]:
    """
    Splits Markdown text semantically by headers, preserving tables and lists.
    Returns: (success, list_of_chunks, failure_reason)
    """
    chunks = []
    current_chunk_blocks = []
    current_char_count = 0

    for block in blocks_cache.get("blocks", []):
        block_len = len(block.get("content", ""))

        if current_char_count + block_len > max_chunk_size and current_chunk_blocks:
            chunks.append(
                _roll_up_chunk(
                    current_chunk_blocks, blocks_cache["rulebookId"], len(chunks)
                )
            )
            current_chunk_blocks = []
            current_char_count = 0

        current_chunk_blocks.append(block)
        current_char_count += block_len

    if current_chunk_blocks:
        chunks.append(
            _roll_up_chunk(
                current_chunk_blocks, blocks_cache["rulebookId"], len(chunks)
            )
        )

    logger.info("Successfully generated %d Markdown-aware chunks.", len(chunks))

    return (True, chunks, "")


def _roll_up_chunk(blocks: list, rulebook_id: str, index: int) -> dict:
    """Derives exposed metadata from constituent blocks."""
    content = "\n".join(b["content"] for b in blocks if b.get("content"))

    return {
        "chunkId": ObjectId(),
        "rulebookId": rulebook_id,
        "index": index,
        "content": content,
        "charCount": len(content),
        "type": _determine_chunk_type(blocks),
        "needsReview": any(b.get("confidence", 1.0) < 0.8 for b in blocks),
        "confidence": min([b.get("confidence", 1.0) for b in blocks] or [1.0]),
        "associatedImageUrls": [b["imageUrl"] for b in blocks if b.get("imageUrl")],
    }


def _determine_chunk_type(blocks: list[dict]) -> str:
    """
    Rolls up constituent block types into a single chunk type
    Types: 'text', 'table', 'image-heavy', 'mixed'
    """
    if not blocks:
        return "text"

    types_present = {b.get("type", "paragraph") for b in blocks}

    if "table" in types_present:
        return "table" if len(types_present) == 1 else "mixed"

    image_count = sum(1 for b in blocks if b.get("type") == "image")
    total_blocks = len(blocks)

    if image_count > 0 and (image_count / total_blocks) >= 0.5:
        return "image-heavy"

    if "image" in types_present:
        return "mixed"

    return "text"
