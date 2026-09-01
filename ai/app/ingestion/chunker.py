import logging

from bson import ObjectId

logger = logging.getLogger(__name__)


class RulebookChunker:
    """
    A custom recursive text chunker. Splits text hierarchically
    to preserve semantic boundaries without exceeding max constraints.
    """

    def __init__(self, max_chunk_size: int = 1000) -> None:
        self.max_chunk_size = max_chunk_size
        self.separators = ["\n\n", "\n", ". ", " "]

    def chunk_text(self, text: str) -> list[str]:
        if not text:
            return []

        text = text.replace("\r\n", "\n")
        return self._split_recursively(text, 0)

    def _split_recursively(self, text: str, separator_index: int) -> list[str]:
        if len(text) <= self.max_chunk_size:
            return [text.strip()]

        if separator_index >= len(self.separators):
            return self._force_split(text)

        separator = self.separators[separator_index]

        if separator not in text:
            return self._split_recursively(text, separator_index + 1)

        return self._process_splits(text.split(separator), separator, separator_index)

    def _process_splits(
        self, split_text: list[str], separator: str, separator_index: int
    ) -> list[str]:
        chunks = []
        current_chunk = ""

        for split in split_text:
            if len(split) > self.max_chunk_size:
                if current_chunk:
                    chunks.append(current_chunk.strip())
                    current_chunk = ""

                sub_chunks = self._split_recursively(split, separator_index + 1)
                chunks.extend(sub_chunks)
                continue

            candidate_chunk_length = (
                len(current_chunk) + len(separator) + len(split)
                if current_chunk
                else len(split)
            )

            if candidate_chunk_length <= self.max_chunk_size:
                current_chunk = self._merge_strings(current_chunk, split, separator)
            else:
                if current_chunk:
                    chunks.append(current_chunk.strip())
                current_chunk = split

        if current_chunk:
            chunks.append(current_chunk.strip())

        return [c for c in chunks if c]

    def _merge_strings(self, current_chunk: str, split: str, separator: str) -> str:
        if not current_chunk:
            return split
        reconstructed_separator = "." if separator == ". " else separator
        return current_chunk + reconstructed_separator + split

    def _force_split(self, text: str) -> list[str]:
        return [
            text[i : i + self.max_chunk_size]
            for i in range(0, len(text), self.max_chunk_size)
        ]


def generate_chunks(full_text: str) -> tuple[bool, list[dict], str]:
    """
    Splits the extracted text into logical chunks for MongoDB and Vectorisation.
    Returns: (success, list_of_chunks, failure_reason)
    """
    try:
        chunker = RulebookChunker(max_chunk_size=1000)
        raw_chunks = chunker.chunk_text(full_text)

        chunks = []

        for current_index, chunk_text in enumerate(raw_chunks):
            if not chunk_text:
                continue

            new_chunk = {
                "chunkId": ObjectId(),
                "index": current_index,
                "content": chunk_text,
                "charCount": len(chunk_text),
            }
            chunks.append(new_chunk)

        if not chunks:
            return (False, [], "Chunks resulted in 0 valid segments")

        logger.info("Successfully generated %d chunks.", len(chunks))
        return (True, chunks, "")
    except Exception:
        logger.exception("Chunking failed")
        return (False, [], "Internal error occurred during text chunking")
