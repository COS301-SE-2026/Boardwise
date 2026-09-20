import logging

from bson import ObjectId

logger = logging.getLogger(__name__)


def generate_chunks(
    blocks_cache: dict, max_chunk_size: int = 1000
) -> tuple[bool, list[dict], str]:
    """
    Splits Markdown text semantically by headers, preserving tables and lists.
    Enforces a strict max_chunk_size by sub-chunking oversized paragraphs
    Returns: (success, list_of_chunks, failure_reason)
    """
    chunks = []
    rulebook_id = blocks_cache.get("rulebookId", "")
    if not rulebook_id:
        logger.error("Rulebook ID not provided with the blocks_cache")
        return (False, [], "Rulebook ID not provided.")

    raw_blocks = blocks_cache.get("blocks", [])
    if not raw_blocks:
        return (True, [], "")

    sections: list[list[dict]] = []
    current_section: list[dict] = []

    for block in raw_blocks:
        if block.get("type") == "heading":
            if current_section:
                sections.append(current_section)
            current_section = [block]
        else:
            current_section.append(block)
    if current_section:
        sections.append(current_section)

    # Process each section into 1 or more chunks based on max_chunk_size
    for section in sections:
        _chunk_section(
            section_blocks=section,
            rulebook_id=rulebook_id,
            chunks=chunks,
            max_chunk_size=max_chunk_size,
        )

    logger.info("Successfully generated %d Markdown-aware chunks.", len(chunks))

    return (True, chunks, "")


def _chunk_section(
    section_blocks: list[dict],
    rulebook_id: str,
    chunks: list[dict],
    max_chunk_size: int,
) -> None:
    """Chunks an individual heading-delimited section, enforcing the max_chunk_size limit"""
    current_chunk_blocks: list[dict] = []
    current_char_count = 0

    for block in section_blocks:
        content = block.get("content", "")
        block_len = len(content)

        if block_len > max_chunk_size:
            if current_chunk_blocks:
                chunks.append(
                    _roll_up_chunk(current_chunk_blocks, rulebook_id, len(chunks))
                )
                current_chunk_blocks = []
                current_char_count = 0

            sub_texts = _split_large_block(content, max_chars=max_chunk_size - 50)

            for index, sub_text in enumerate(sub_texts):
                sub_block = block.copy()
                sub_block["content"] = sub_text

                # Prevents image duplication by only attaching the image to the first sub-chunk
                if index > 0 and "imageUrl" in sub_block:
                    sub_block.pop("imageUrl", None)

                chunks.append(_roll_up_chunk([sub_block], rulebook_id, len(chunks)))
            continue

        # Adding 1 to account for the "\n" joiner used in _roll_up_chunk
        added_len = block_len + (1 if current_chunk_blocks else 0)
        if current_char_count + added_len > max_chunk_size:
            chunks.append(
                _roll_up_chunk(current_chunk_blocks, rulebook_id, len(chunks))
            )
            current_chunk_blocks = []
            current_char_count = 0
            added_len = (
                block_len  # Reset because this is now the first block in the queue
            )

        current_chunk_blocks.append(block)
        current_char_count += added_len

    if current_chunk_blocks:
        chunks.append(_roll_up_chunk(current_chunk_blocks, rulebook_id, len(chunks)))


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
    Types: 'text', 'table', 'image-heavy', 'mixed', 'decorative'
    """
    if not blocks:
        return "text"

    types_present = {b.get("type", "paragraph") for b in blocks}
    total_blocks = len(blocks)

    decorative_count = sum(1 for b in blocks if b.get("type") == "decorative")
    if decorative_count > 0 and (decorative_count / total_blocks) >= 0.5:
        return "decorative"

    if "table" in types_present:
        return "table" if len(types_present) == 1 else "mixed"

    image_count = sum(1 for b in blocks if b.get("type") == "image")
    if image_count > 0 and (image_count / total_blocks) >= 0.5:
        return "image-heavy"

    if "image" in types_present:
        return "mixed"

    return "text"


def _split_large_block(block_text, max_chars=950) -> list:
    """Helper function to handle oversized blocks"""
    sentences = block_text.replace("\n", " ").split(". ")
    sub_chunks = []
    current_chunk = ""

    for sentence in sentences:
        sentence = sentence.strip()
        if not sentence:
            continue

        if not sentence.endswith("."):
            sentence += "."

        if len(current_chunk) + len(sentence) + 1 <= max_chars:
            current_chunk += sentence + " "
        else:
            if current_chunk:
                sub_chunks.append(current_chunk.strip())

            # Handling the rare edge case where a single sentence is longer than max_chars
            if len(sentence) > max_chars:
                sub_chunks.append(sentence[:max_chars])
                current_chunk = sentence[max_chars:] + " "
            else:
                current_chunk = sentence + " "
    if current_chunk:
        sub_chunks.append(current_chunk.strip())

    return sub_chunks
