import logging
import re

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
    current_section_kind = None  # None | aside

    for block in raw_blocks:
        block_kind = "aside" if block.get("type") == "aside" else None
        is_heading = block.get("type") == "heading"

        if (is_heading or block_kind != current_section_kind) and current_section:
            sections.append(current_section)
            current_section = []

        current_section.append(block)
        current_section_kind = block_kind
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


def filter_out_decorative_chunks(chunk_list: list[dict]) -> list[dict]:
    """
    Filters out decorative chunks (trademark lines, all-caps title-page noise
    and any chunk that is still over 50% throwaway image content)
    """
    return [c for c in chunk_list if c.get("type") != "decorative"]


def _handle_oversized_block(
    block: dict, content: str, rulebook_id: str, chunks: list[dict], max_chunk_size: int
) -> None:
    """Process a single block that exceeds the maximum chunk size."""
    sub_texts = _split_large_block(content, max_chars=max_chunk_size - 50)

    for sub_text in sub_texts:
        sub_block = block.copy()
        sub_block["content"] = sub_text

        chunks.append(_roll_up_chunk([sub_block], rulebook_id, len(chunks)))


def _chunk_section(  # NOSONAR
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

            _handle_oversized_block(block, content, rulebook_id, chunks, max_chunk_size)
            continue

        # Adding 1 to account for the "\n" joiner used in _roll_up_chunk
        added_len = block_len + (1 if current_chunk_blocks else 0)
        if current_char_count + added_len > max_chunk_size:
            chunks.append(
                _roll_up_chunk(current_chunk_blocks, rulebook_id, len(chunks))
            )

            overlap_block = current_chunk_blocks[-1:] if current_chunk_blocks else []
            current_chunk_blocks = overlap_block
            current_char_count = sum(
                len(b.get("content", "")) for b in current_chunk_blocks
            ) + len(overlap_block)

            added_len = block_len + (1 if current_chunk_blocks else 0)

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
        "needsReview": any(
            b.get("confidence", 1.0) < 0.8 or b.get("forceReview", False)
            for b in blocks
        ),
        "confidence": min([b.get("confidence", 1.0) for b in blocks] or [1.0]),
    }


def _determine_chunk_type(blocks: list[dict]) -> str:
    """
    Rolls up constituent block types into a single chunk type
    Types: 'text', 'table', 'aside', 'mixed', 'decorative'
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

    aside_count = sum(1 for b in blocks if b.get("type") == "aside")
    if aside_count > 0 and (aside_count / total_blocks) >= 0.5:
        return "aside"

    return "text"


def _split_large_block(block_text, max_chars=950) -> list:
    """Helper function to handle oversized blocks using sentence-aware boundaries and 1-sentence overlap"""
    sentences = [
        s.strip()
        for s in re.split(r"(?<=[.!?])\s+", block_text.replace("\n", " "))
        if s.strip()
    ]
    sub_chunks = []
    current_sentences = []
    current_len = 0

    for sentence in sentences:
        if current_len + len(sentence) + 1 <= max_chars:
            current_sentences.append(sentence)
            current_len += len(sentence) + 1
        else:
            if current_sentences:
                sub_chunks.append(" ".join(current_sentences))

            # Carry over the last sentence of the previous sub-chunk
            overlap = [current_sentences[-1]] if current_sentences else []

            if len(sentence) > max_chars:
                # Handling the rare edge case where a single sentence is longer than max_chars
                sub_chunks.append(sentence[:max_chars])
                current_sentences = [sentence[max_chars:]]
                current_len = len(current_sentences[0]) + 1
            else:
                current_sentences = overlap + [sentence]
                current_len = sum(len(s) for s in current_sentences) + len(
                    current_sentences
                )

    if current_sentences:
        sub_chunks.append(" ".join(current_sentences).strip())

    return sub_chunks
