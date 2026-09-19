import logging
from datetime import datetime, timezone

import lancedb
import pyarrow as pa

from app.config import settings

logger = logging.getLogger(__name__)

_db = None
_table = None


def _build_schema() -> pa.schema:
    return pa.schema(
        [
            pa.field("chunkId", pa.string()),
            pa.field("rulebookId", pa.string()),
            pa.field("content", pa.string()),
            pa.field("index", pa.int32()),
            pa.field("charCount", pa.int32()),
            pa.field("vector", pa.list_(pa.float32(), settings.EMBEDDING_DIMENSIONS)),
            pa.field("createdAt", pa.timestamp("us")),
            pa.field("updatedAt", pa.timestamp("us")),
        ]
    )


def get_db():
    """Returns a cached LanceDB connection."""
    global _db
    if _db is None:
        _db = lancedb.connect(settings.LANCEDB_URI)
    return _db


def get_table():
    """
    Returns a cached handle to the shared rulebook_text table,
    creating it if it doesn't exist yet.
    """
    global _table
    if _table is not None:
        return _table

    db = get_db()
    table_name = settings.LANCEDB_TABLE_NAME

    if table_name in db.table_names():
        _table = db.open_table(table_name)
    else:
        logger.info("LanceDB table '%s' does not exist. Creating table.", table_name)
        _table = db.create_table(table_name, schema=_build_schema())

    return _table


def ensure_indexes(force_recreate: bool = False) -> None:
    """
    Builds the IVF vector index and the FTS index on 'content'.
    """
    table = get_table()

    logger.info(
        "Building IVF vector index on 'vector' (%d partitions)",
        settings.LANCEDB_IVF_PARTITIONS,
    )

    try:
        table.create_index(
            metric="cosine",
            vector_column_name="vector",
            index_type="IVF_FLAT",
            num_partitions=settings.LANCEDB_IVF_PARTITIONS,
            replace=force_recreate,
        )
    except RuntimeError as e:
        if "without training" in str(e) or "empty" in str(e).lower():
            logger.warning(
                "Skipped vector index creation: Table is empty or too small to train the index. "
                "LanceDB will use exact KNN search instead"
            )
        else:
            raise

    logger.info("Building FTS index on 'content'")
    try:
        table.create_fts_index("content", replace=True)
        logger.info("LanceDB indexes ready.")
    except Exception as e:
        logger.error(f"Failed to create FTS index: {e}.")


def ping_lancedb() -> None:
    """Startup health check"""
    get_table()
    logger.info("LanceDB connection verified")


def is_index_ready() -> bool:
    """Checks if the table exists and both vector and FTS indexes are present"""
    try:
        table = get_table()
        indices = [idx.name for idx in table.list_indices()]
        has_vector_idx = any("vector" in idx for idx in indices)
        has_fts_idx = any("content" in idx for idx in indices)
        return has_vector_idx and has_fts_idx
    except Exception:
        logger.exception("Failed LanceDB index readiness check.")
        return False


def write_chunks(chunks: list[dict]) -> None:
    """
    Bulk-inserts newly ingested chunks.
    """
    if not chunks:
        return

    table = get_table()

    rows = [
        {
            "chunkId": str(chunk["chunkId"]),
            "rulebookId": str(chunk["rulebookId"]),
            "content": chunk["content"],
            "index": int(chunk["index"]),
            "charCount": int(chunk["charCount"]),
            "vector": chunk["embedding"],
            "createdAt": chunk.get("createdAt", datetime.now(timezone.utc)),
            "updatedAt": chunk.get("updateAt", datetime.now(timezone.utc)),
        }
        for chunk in chunks
    ]

    table.add(rows)
    logger.info("Wrote %d chunk vectors to LanceDB.", len(rows))


def upsert_chunk(
    chunk_id: str, rulebook_id: str, content: str, index: int, embedding: list[float]
) -> None:
    """Upserts a single chunk's vector + content."""
    table = get_table()
    now = datetime.now(timezone.utc)

    row = pa.Table.from_pylist(
        [
            {
                "chunkId": str(chunk_id),
                "rulebookId": str(rulebook_id),
                "content": content,
                "index": int(index),
                "charCount": len(content),
                "vector": embedding,
                "createdAt": now,
                "updatedAt": now,
            }
        ],
        schema=_build_schema(),
    )

    (
        table.merge_insert("chunkId")
        .when_matched_update_all()
        .when_not_matched_insert_all()
        .execute(row)
    )

    logger.info("Upserted chunk %s into LanceDB", chunk_id)


def query_vector(rulebook_id: str, query_vector: list[float], limit: int) -> list[dict]:
    """
    Vector-similarity search part of the hybrid retrieval
    """
    table = get_table()
    results = (
        table.search(query_vector, vector_column_name="vector")
        .where(f"rulebookId = '{rulebook_id}'", prefilter=True)
        .limit(limit)
        .select(["chunkId", "content", "index", "charCount", "_distance"])
        .to_list()
    )
    return results


def query_fts(rulebook_id: str, query_text: str, limit: int) -> list[dict]:
    """
    Lexical search part of the hybrid retrieval
    """
    table = get_table()
    results = (
        table.search(query_text, query_type="fts")
        .where(f"rulebookId = '{rulebook_id}'", prefilter=True)
        .limit(limit)
        .select(["chunkId", "content", "index", "charCount"])
        .to_list()
    )
    return results


def rulebook_has_vectors(rulebook_id: str) -> bool:
    """
    Used to confirm if a specific rulebook actually has rows in LanceDB
    """
    table = get_table()
    matches = (
        table.search()
        .where(f"rulebookId = '{rulebook_id}'", prefilter=True)
        .limit(1)
        .to_list()
    )
    return len(matches) > 0


def delete_chunks(chunk_ids: list[str]) -> None:
    """
    Deletes specific chunk vectors from LanceDB.
    Required to clean up orphans created by mongo chunk deletions.
    """
    if not chunk_ids:
        return

    table = get_table()
    formatted_ids = ", ".join([f"'{cid}'" for cid in chunk_ids])

    table.delete(f"chunkId IN ({formatted_ids})")
    logger.info("Deleted %d orphaned chunks from LanceDB.", len(chunk_ids))


def get_all_chunk_ids_for_rulebook(rulebook_id: str) -> set[str]:
    """Retrieves all chunk IDs indexed in LanceDB for the specific rulebook"""
    table = get_table()
    results = (
        table.search()
        .where(f"rulebookId = '{rulebook_id}'", prefilter=True)
        .select(["chunkId"])
        .to_list()
    )
    return {r["chunkId"] for r in results}


def run_scheduled_compaction() -> None:
    """
    Compact file fragments generated by upserts and monitors fragment count.
    Should be triggered via a scheduled background task
    """
    table = get_table()

    try:
        fragments_before = len(table.to_lance().get_fragments())
        logger.info(
            "LanceDB table has %d fragments before compaction.", fragments_before
        )
    except Exception as e:
        logger.warning("Could not retrieve initial fragment count: %s", e)

    logger.info("Initiating LanceDB file compaction and version cleanup")
    table.compact_files()
    table.cleanup_old_versions()
    try:
        fragments_after = len(table.to_lance().get_fragments())
        logger.info("LanceDB table has %d fragments after compaction.", fragments_after)
    except Exception:
        pass
