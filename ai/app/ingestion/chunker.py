import logging
import re

from bson import ObjectId

logger = logging.getLogger(__name__)

MIN_CHUNK_CHARS = 20
MIN_ALPHA_RATIO = 0.4
SHORT_LINE_CHAR_LIMIT = 4
SHORT_LINE_RATIO_THRESHOLD = 0.6
MIN_SHORT_LINE_STACK = 3

_HEADER_LINE_PATTERN = re.compile(r"^(#{1,6})[ \t]+(.*)$", re.MULTILINE)
_TABLE_ROW_PATTERN = re.compile(r"^\s*\|.+\|\s*$", re.MULTILINE)
_TABLE_SEPARATOR_PATTERN = re.compile(
    r"^[ \t]*\|?[ \t]*:?-{2,}:?(?:[ \t]*\|[ \t]*:?-{2,}:?)*[ \t]*\|?[ \t]*$",
    re.MULTILINE,
)

_FILENAME_ARTIFACT_PATTERNS = (
    re.compile(r"\.(indd|pdf|qxd|idml)\b", re.IGNORECASE),
    re.compile(r"\b\d{2}[./-]\d{2}[./-]\d{2,4}\b"),
    re.compile(r"\b(seite|page|p\.)\s*\d+\b", re.IGNORECASE),
)

_RULEBOOK_BOILERPLATE_PATTERNS = (
    re.compile(r"\bAll Rights Reserved\b", re.IGNORECASE),
    re.compile(r"\bis a registered trademark\b", re.IGNORECASE),
    re.compile(r"\bNo part of this (product|book|game)\b", re.IGNORECASE),
    re.compile(r"\bNOT INTENDED FOR USE BY PERSONS\b", re.IGNORECASE),
    re.compile(r"\bMade in (China|Germany|U\.S\.A\.|USA)\b", re.IGNORECASE),
    re.compile(
        r"\b(Consumer contact|Consumer Relations|Customer Service)\b", re.IGNORECASE
    ),
    re.compile(r"©\s*\d{4}"),
)


def generate_chunks(
    pages_cache: dict, max_chunk_size: int = 1000
) -> tuple[bool, list[dict], str]:
    """
    Splits Markdown text semantically by headers, preserving tables and lists.
    Enforces a strict max_chunk_size by sub-chunking oversized paragraphs
    Returns: (success, list_of_chunks, failure_reason)
    """
    rulebook_id = pages_cache.get("rulebookId", "")
    if not rulebook_id:
        logger.error("Rulebook ID not provided with the pages_cache")
        return (False, [], "Rulebook ID not provided.")

    pages = pages_cache.get("pages", [])
    if not pages:
        return (True, [], "")

    document_text, page_offsets = _build_document_with_page_offsets(pages)
    if not document_text.strip():
        return (True, [], "")

    chunks: list[dict] = []
    for section_text, metadata, page_numbers in _split_by_headers(
        document_text, page_offsets
    ):
        _chunk_section(
            section_text=section_text,
            metadata=metadata,
            page_numbers=page_numbers,
            rulebook_id=rulebook_id,
            chunks=chunks,
            max_chunk_size=max_chunk_size,
            pages=pages,
        )

    logger.info("Successfully generated %d Markdown-aware chunks.", len(chunks))

    return (True, chunks, "")


def filter_out_decorative_chunks(chunk_list: list[dict]) -> list[dict]:
    """
    Filters out low-quality chunks: filename/production artifacts,
    legal boilerplate, and other noise that survived the header/footer stripping
    """
    return [
        c
        for c in chunk_list
        if not is_low_quality_rulebook_content(c.get("content", ""))
    ]


def is_low_quality_rulebook_content(content: str) -> bool:
    """Determines if content is low quality based on certain heuristics"""
    text = content.strip()
    if not text:
        return True
    if len(text) < MIN_CHUNK_CHARS:
        return True
    if _matches_any(text, _FILENAME_ARTIFACT_PATTERNS):
        return True
    if _matches_any(text, _RULEBOOK_BOILERPLATE_PATTERNS):
        return True
    if _alpha_ratio(text) < MIN_ALPHA_RATIO:
        return True
    return _is_short_line_stack(text)


def _matches_any(text: str, patterns) -> bool:
    return any(p.search(text) for p in patterns)


def _alpha_ratio(text: str) -> float:
    return sum(1 for c in text if c.isalpha()) / len(text)


def _is_short_line_stack(text: str) -> bool:
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    if len(lines) < MIN_SHORT_LINE_STACK:
        return False
    short = sum(1 for line in lines if len(line) <= SHORT_LINE_CHAR_LIMIT)
    return (short / len(lines)) >= SHORT_LINE_RATIO_THRESHOLD


def _build_document_with_page_offsets(
    pages: list[dict],
) -> tuple[str, list[tuple[int, int]]]:
    """
    Concatenates page Markdown into one document.
    Returns the text alongside a list of (start_offset, page_number) pairs marking where each page's content begins
    """
    parts = []
    offsets: list[tuple[int, int]] = []
    cursor = 0

    for page in pages:
        markdown_text = page.get("markdown", "")
        if not markdown_text.strip():
            continue
        if parts:
            cursor += 2  # account for the "\n\n" joiner before this page
        offsets.append((cursor, page["page"]))
        parts.append(markdown_text)
        cursor += len(markdown_text)
    return ("\n\n".join(parts), offsets)


def _pages_for_range(
    page_offsets: list[tuple[int, int]], start: int, end: int
) -> list[int]:
    """Returns every page whose start_offset - next_start_offset rage overlaps the given start - end text range."""
    result = []
    for i, (page_start, page_number) in enumerate(page_offsets):
        page_end = page_offsets[i + 1][0] if i + 1 < len(page_offsets) else float("inf")
        if page_start < end and page_end > start:
            result.append(page_number)
    return result


def _split_by_headers(
    document_text: str, page_offsets: list[tuple[int, int]]
) -> list[tuple[str, dict[str, str], list[int]]]:
    """Splits Markdown heading lines"""
    matches = list(_HEADER_LINE_PATTERN.finditer(document_text))
    if not matches:
        if document_text.strip():
            return [
                (
                    document_text,
                    {},
                    _pages_for_range(page_offsets, 0, len(document_text)),
                )
            ]
        return []

    sections: list[tuple[str, dict[str, str], list[int]]] = []
    first_start = matches[0].start()
    if first_start > 0 and document_text[:first_start].strip():
        sections.append(
            (
                document_text[:first_start],
                {},
                _pages_for_range(page_offsets, 0, first_start),
            )
        )

    active_headers: dict[int, str] = {}
    for i, match in enumerate(matches):
        level = len(match.group(1))
        title = match.group(2).strip()

        for existing_level in [lvl for lvl in active_headers if lvl >= level]:
            del active_headers[existing_level]
        active_headers[level] = title

        breadcrumb = {
            f"Header {lvl}": active_headers[lvl] for lvl in sorted(active_headers)
        }

        start = match.start()
        end = matches[i + 1].start() if i + 1 < len(matches) else len(document_text)
        sections.append(
            (
                document_text[start:end],
                breadcrumb,
                _pages_for_range(page_offsets, start, end),
            )
        )

    return sections


def _classify_section_type(text: str) -> str:
    """Types: 'table', 'aside', or 'text'."""
    if _TABLE_ROW_PATTERN.search(text) and _TABLE_SEPARATOR_PATTERN.search(text):
        return "table"

    lines = [line for line in text.splitlines() if line.strip()]
    if lines:
        aside_lines = sum(1 for line in lines if line.strip().startswith(">"))
        if aside_lines / len(lines) >= 0.6:
            return "aside"
    return "text"


def _get_page_confidence(
    pages: list[dict], page_numbers: list[int]
) -> tuple[float, bool]:
    """Rolls up the confidence/flagged state of a section's source page(s)"""
    if not page_numbers:
        return (1.0, False)
    matching = [p for p in pages if p.get("page") in page_numbers]
    if not matching:
        return (1.0, False)
    confidence = min(p.get("confidence", 1.0) for p in matching)
    needs_review = any(p.get("flagged") for p in matching) or confidence < 0.8
    return (confidence, needs_review)


def _chunk_section(
    section_text: str,
    metadata: dict[str, str],
    page_numbers: list[int],
    rulebook_id: str,
    chunks: list[dict],
    max_chunk_size: int,
    pages: list[dict],
) -> None:
    """
    Chunks an individual heading-delimited section, enforcing the max_chunk_size limit.
    Sub-chunks of a section share it's header breadcrumb and page range
    """
    content = section_text.strip()
    if not content:
        return

    confidence, needs_review = _get_page_confidence(pages, page_numbers)

    if len(content) <= max_chunk_size:
        chunks.append(
            _make_chunk(
                content,
                metadata,
                rulebook_id,
                len(chunks),
                page_numbers,
                confidence,
                needs_review,
            )
        )
        return

    for sub_text in _split_large_block(content, max_chars=max_chunk_size - 50):
        chunks.append(
            _make_chunk(
                sub_text,
                metadata,
                rulebook_id,
                len(chunks),
                page_numbers,
                confidence,
                needs_review,
            )
        )


def _make_chunk(
    content: str,
    metadata: dict[str, str],
    rulebook_id: str,
    index: int,
    page_numbers: list[int],
    confidence: float,
    needs_review: bool,
) -> dict:
    return {
        "chunkId": ObjectId(),
        "rulebookId": rulebook_id,
        "index": index,
        "content": content,
        "charCount": len(content),
        "type": _classify_section_type(content),
        "sourcePages": page_numbers,
        "metadata": metadata,
        "needsReview": needs_review,
        "confidence": confidence,
    }


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
