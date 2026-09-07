import logging

from bson import ObjectId
from langchain_text_splitters import (
    Language,
    MarkdownHeaderTextSplitter,
    RecursiveCharacterTextSplitter,
)

logger = logging.getLogger(__name__)


def generate_chunks(
    full_text: str, max_chunk_size: int = 1000
) -> tuple[bool, list[dict], str]:
    """
    Splits Markdown text semantically by headers, preserving tables and lists.
    Returns: (success, list_of_chunks, failure_reason)
    """
    if not full_text:
        return (False, [], "No text provided for chunking.")

    try:
        headers_to_split_on = [
            ("#", "Header 1"),
            ("##", "Header 2"),
            ("###", "Header 3"),
        ]

        markdown_splitter = MarkdownHeaderTextSplitter(
            headers_to_split_on=headers_to_split_on,
            strip_headers=False,  # Keep headers in the text so frontend rendering stays intact
        )
        header_splits = markdown_splitter.split_text(full_text)

        recursive_splitter = RecursiveCharacterTextSplitter.from_language(
            language=Language.MARKDOWN, chunk_size=max_chunk_size, chunk_overlap=100
        )
        final_splits = recursive_splitter.split_documents(header_splits)

        chunks = []
        for current_index, doc in enumerate(final_splits):
            chunk_text = doc.page_content.strip()
            if not chunk_text:
                continue

            new_chunk = {
                "chunkId": ObjectId(),
                "index": current_index,
                "content": chunk_text,
                "charCount": len(chunk_text),
                "metadata": doc.metadata,
            }
            chunks.append(new_chunk)

        if not chunks:
            return (False, [], "Chunks resulted in 0 valid segments")

        logger.info("Successfully generated %d Markdown-aware chunks.", len(chunks))
        return (True, chunks, "")
    except Exception:
        logger.exception("Chunking failed")
        return (False, [], "Internal error occurred during text chunking")
