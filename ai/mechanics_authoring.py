#!/usr/bin/env python3
"""Authoring tool for the MECHANIC collection (Group 1, Generative Game Architect).

Workflow (each step is a subcommand):

  1. seed    Upsert MECHANIC docs from SEED_MECHANICS:
             {_id: slug, nameKey, name, bggId, description, descriptionSource}.
             Never touches requiresComponentTypes; never writes category.
  2. draft   For docs where requiresComponentTypes is absent, ask the LLM (in
             batches) to classify them against the ComponentType enum, using
             the doc's existing hand-authored description as context. Writes a
             review file only; nothing touches Mongo.
  3. (you)   Open the review file, fix anything wrong, set "approved": true.
  4. apply   Push approved entries into Mongo ($set of requiresComponentTypes
             and provenance).
  5. report  Show docs still missing requiresComponentTypes / bggId / description,
             plus any legacy integer-keyed docs.

Run from the backend project root so `app.*` imports resolve, e.g.:

  python mechanics_authoring.py seed
  python mechanics_authoring.py draft --out mechanic_drafts.json --limit 10
  python mechanics_authoring.py apply --file mechanic_drafts.json --dry-run
  python mechanics_authoring.py apply --file mechanic_drafts.json
  python mechanics_authoring.py report

Mongo connection: uses app.utils.mongo_service.get_db().

IMPORTANT: slug() must match the slug function used on the Java side. Both must
follow §3.2: lowercase, runs of non-[a-z0-9] -> "-", trim "-". Do NOT truncate
mechanic nameKeys; only component/game slugs are short-bounded.
"""

import argparse
import json
import logging
import re
import time
from datetime import datetime, timezone
from pathlib import Path

from mechanics_data import SEED_MECHANICS
from app.services.mongo_service import get_db

logger = logging.getLogger("mechanics_authoring")

COLLECTION = "MECHANIC"
COMPONENT_TYPES = [
    "board",
    "card",
    "die",
    "token",
    "meeple",
    "tile",
    "miniature",
    "other",
]
DESC_MIN, DESC_MAX = 30, 350

# Matches the spec in §3.2. For any mechanic name < 200 chars, slug(name) is
# byte-identical to app.utils.ggaia_utils.slugify(name, 200). Do not lower it:
# longer "Turn Order: ..." and "Worker Placement, ..." names collide at 16.
SLUG_MAX_LEN = 200

# Do NOT include requiresComponentTypes == [] in this filter. An empty list is a
# legitimate, reviewed answer meaning "no specific components required"; only an
# absent (or null) field means "not yet drafted". Re-drafting [] silently
# destroys reviewed data.
MISSING_REQUIRES_TYPES = {
    "$or": [
        {"requiresComponentTypes": {"$exists": False}},
        {"requiresComponentTypes": None},
    ]
}

MISSING_DESCRIPTION = {
    "$or": [
        {"description": {"$exists": False}},
        {"description": None},
        {"description": ""},
    ]
}

DRAFT_SCHEMA = {
    "type": "object",
    "properties": {
        "mechanics": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "nameKey": {"type": "string"},
                    "requiresComponentTypes": {
                        "type": "array",
                        "items": {"type": "string", "enum": COMPONENT_TYPES},
                    },
                },
                "required": ["nameKey", "requiresComponentTypes"],
            },
        }
    },
    "required": ["mechanics"],
}

SYSTEM_PROMPT = (
    "You are classifying board game mechanics for a game-design tool. "
    "Be conservative and factual. Only list a component type when the mechanic's "
    "definition names a physical item of that type as essential."
)


# --------------------------------------------------------------------------- #
# helpers
# --------------------------------------------------------------------------- #


def slug(name: str) -> str:
    """Slugify a mechanic name for use as _id / nameKey.

    Must equal app.utils.ggaia_utils.slugify(name, SLUG_MAX_LEN) exactly.
    """
    return re.sub(r"[^a-z0-9]+", "-", name.lower()).strip("-")


def now() -> datetime:
    return datetime.now(timezone.utc)


def _trim_description(desc: str) -> str:
    """Trim an over-length description at a word boundary, dropping trailing
    punctuation."""
    cut = desc[:DESC_MAX].rsplit(" ", 1)[0].rstrip(".,;:")
    return cut or desc[:DESC_MAX]


# --------------------------------------------------------------------------- #
# seed
# --------------------------------------------------------------------------- #


def cmd_seed(args) -> None:
    col = get_db()[COLLECTION]

    by_key: dict[str, dict] = {}
    seen_bgg: dict[int, str] = {}

    for entry in SEED_MECHANICS:
        name = entry["name"]
        bgg_id = entry["mechanicId"]
        key = slug(name)

        if not key:
            logger.warning("Skipping %r: empty slug.", name)
            continue
        if key in by_key and by_key[key]["name"] != name:
            logger.warning(
                "Slug collision: %r and %r -> %r (keeping first).",
                by_key[key]["name"],
                name,
                key,
            )
            continue
        if bgg_id in seen_bgg:
            logger.warning(
                "Duplicate bggId %s: %r and %r (keeping first).",
                bgg_id,
                seen_bgg[bgg_id],
                name,
            )
            continue

        by_key[key] = entry
        seen_bgg[bgg_id] = name

    logger.info(
        "Loaded %d seed mechanics (%d distinct slugs).",
        len(SEED_MECHANICS),
        len(by_key),
    )

    if args.dry_run:
        for key, entry in sorted(by_key.items()):
            print(f"{key:50s} bggId={entry['mechanicId']}")
        return

    inserted = updated = 0
    for key, entry in by_key.items():
        name = entry["name"]
        bgg_id = entry["mechanicId"]
        desc = entry["description"].strip()

        if len(desc) > DESC_MAX:
            trimmed = _trim_description(desc)
            logger.warning(
                "Description for %r is %d chars; trimmed to %d.",
                name,
                len(desc),
                len(trimmed),
            )
            desc = trimmed
        elif len(desc) < DESC_MIN:
            logger.warning(
                "Description for %r is only %d chars (< %d); keeping as-is.",
                name,
                len(desc),
                DESC_MIN,
            )

        try:
            res = col.update_one(
                {"nameKey": key},
                {
                    "$setOnInsert": {
                        "_id": key,
                        "nameKey": key,
                        "name": name,
                        "createdAt": now(),
                    },
                    "$set": {
                        "bggId": bgg_id,
                        "description": desc,
                        "descriptionSource": "hand-authored",
                        "updatedAt": now(),
                    },
                },
                upsert=True,
            )
        except Exception:
            # Most likely a DuplicateKeyError on the unique sparse bggId index,
            # which means a *different* doc already owns this bggId.
            logger.exception("Failed to upsert %r (bggId=%s).", name, bgg_id)
            continue

        if res.upserted_id is not None:
            inserted += 1
        else:
            updated += 1

    logger.info("Seed complete: %d inserted, %d updated.", inserted, updated)


# --------------------------------------------------------------------------- #
# draft
# --------------------------------------------------------------------------- #


def build_prompt(batch: list[dict]) -> str:
    entries = []
    for m in batch:
        entries.append(
            f'- nameKey: "{m["nameKey"]}"\n'
            f'  name: "{m["name"]}"\n'
            f'  description: "{m["description"]}"'
        )
    listing = "\n\n".join(entries)
    return f"""For each board game mechanic below, decide the MINIMAL set of physical
component types a game must own for the mechanic to be usable.

Allowed values only: {", ".join(COMPONENT_TYPES)}.

Rules:
- Include a type ONLY when the mechanic's definition names a physical item of
  that type as essential.
  Examples: Dice Rolling -> ["die"]; Hand Management -> ["card"];
            Tile Placement -> ["tile"]; Grid Movement -> ["board"].
- If the mechanic can be implemented with a substitutable item (tokens, dial,
  paper), leave it out. Example: Action Points -> [].
- Do NOT add "board" just because most games have one.
- Use [] if no specific component type is essential.
- "other" is for essential components that fit none of the other types.

Return ONE entry per mechanic, using the exact nameKey given, with ONLY the
fields nameKey and requiresComponentTypes.

Mechanics:
{listing}
"""


def validate_entry(entry: dict, expected_keys: set[str]) -> tuple[bool, str]:
    key = entry.get("nameKey")
    if key not in expected_keys:
        return False, f"unexpected_name_key:{key}"
    types = entry.get("requiresComponentTypes")
    if not isinstance(types, list):
        return False, "types_not_list"
    bad = [t for t in types if t not in COMPONENT_TYPES]
    if bad:
        return False, f"invalid_types:{bad}"
    return True, ""


def load_review_file(path: Path) -> dict[str, dict]:
    if not path.exists():
        return {}
    with open(path, encoding="utf-8") as f:
        return {e["nameKey"]: e for e in json.load(f)}


def save_review_file(path: Path, entries: dict[str, dict]) -> None:
    tmp = path.with_suffix(path.suffix + ".tmp")
    with open(tmp, "w", encoding="utf-8") as f:
        json.dump(
            sorted(entries.values(), key=lambda e: e["nameKey"]),
            f,
            indent=2,
            ensure_ascii=False,
        )
    tmp.replace(path)


def cmd_draft(args) -> None:
    from app.ingestion.language_models.llm_client import call_structured_llm

    col = get_db()[COLLECTION]
    out_path = Path(args.out)
    drafts = load_review_file(out_path)

    stubs = list(
        col.find(
            MISSING_REQUIRES_TYPES,
            {"nameKey": 1, "name": 1, "description": 1},
        ).sort("nameKey", 1)
    )
    # Only draft docs that have a description to classify against.
    stubs = [s for s in stubs if (s.get("description") or "").strip()]
    pending = [s for s in stubs if drafts.get(s["nameKey"], {}).get("status") != "ok"]
    if args.limit:
        pending = pending[: args.limit]
    logger.info(
        "%d docs lack requiresComponentTypes; %d to draft this run.",
        len(stubs),
        len(pending),
    )

    for i in range(0, len(pending), args.batch_size):
        batch = pending[i : i + args.batch_size]
        expected = {m["nameKey"] for m in batch}
        names = {m["nameKey"]: m["name"] for m in batch}
        descriptions = {m["nameKey"]: m.get("description", "") for m in batch}

        success, parsed, reason = call_structured_llm(
            build_prompt(batch), DRAFT_SCHEMA, system_prompt=SYSTEM_PROMPT
        )
        if not success:
            logger.error("Batch %d failed: %s", i // args.batch_size + 1, reason)
            for key in expected:
                drafts[key] = {
                    "nameKey": key,
                    "name": names[key],
                    "description": descriptions[key],
                    "status": "failed",
                    "failReason": f"llm_call:{reason}",
                    "approved": False,
                }
            save_review_file(out_path, drafts)
            time.sleep(args.sleep)
            continue

        returned = {}
        for entry in parsed.get("mechanics", []):
            ok, why = validate_entry(entry, expected)
            key = entry.get("nameKey")
            if ok:
                returned[key] = {
                    "nameKey": key,
                    "name": names[key],
                    "description": descriptions[key],
                    "requiresComponentTypes": sorted(
                        set(entry["requiresComponentTypes"])
                    ),
                    "status": "ok",
                    "approved": False,
                }
            else:
                logger.warning("Rejected draft for %s: %s", key, why)
                if key in expected:
                    returned[key] = {
                        "nameKey": key,
                        "name": names[key],
                        "description": descriptions[key],
                        "status": "failed",
                        "failReason": why,
                        "approved": False,
                    }

        for key in expected - returned.keys():
            logger.warning("LLM omitted %s; will retry next run.", key)
        drafts.update(returned)
        save_review_file(out_path, drafts)
        logger.info(
            "Batch %d/%d done.",
            i // args.batch_size + 1,
            -(-len(pending) // args.batch_size),
        )
        time.sleep(args.sleep)

    ok_count = sum(1 for d in drafts.values() if d["status"] == "ok")
    fail_count = sum(1 for d in drafts.values() if d["status"] == "failed")
    logger.info(
        "Review file %s: %d ok, %d failed. Edit it, set approved=true, then run apply.",
        out_path,
        ok_count,
        fail_count,
    )


# --------------------------------------------------------------------------- #
# apply
# --------------------------------------------------------------------------- #


def cmd_apply(args) -> None:
    with open(args.file, encoding="utf-8") as f:
        entries = json.load(f)

    candidates = [
        e
        for e in entries
        if e.get("status") == "ok" and (args.approve_all or e.get("approved") is True)
    ]
    logger.info("%d of %d entries selected for apply.", len(candidates), len(entries))

    # Re-validate: the file was hand-edited, so don't trust it.
    valid = []
    for e in candidates:
        ok, why = validate_entry(e, {e["nameKey"]})
        if ok:
            valid.append(e)
        else:
            logger.warning("Skipping %s after edit: %s", e["nameKey"], why)

    if args.dry_run:
        for e in valid:
            print(f"{e['nameKey']:50s} {e['requiresComponentTypes']}")
        logger.info("Dry run: %d would be applied.", len(valid))
        return

    col = get_db()[COLLECTION]
    applied = missing = 0
    for e in valid:
        res = col.update_one(
            {"nameKey": e["nameKey"]},
            {
                "$set": {
                    "requiresComponentTypes": sorted(set(e["requiresComponentTypes"])),
                    "requiresComponentTypesSource": "llm-draft-reviewed",
                    "updatedAt": now(),
                }
            },
        )
        if res.matched_count:
            applied += 1
        else:
            missing += 1
            logger.warning("No MECHANIC doc for %s (seed first).", e["nameKey"])
    logger.info("Applied %d; %d had no matching doc.", applied, missing)


# --------------------------------------------------------------------------- #
# report
# --------------------------------------------------------------------------- #


def cmd_report(args) -> None:
    col = get_db()[COLLECTION]
    total = col.count_documents({})
    no_types = col.count_documents(MISSING_REQUIRES_TYPES)
    no_bgg = col.count_documents(
        {"$or": [{"bggId": {"$exists": False}}, {"bggId": None}]}
    )
    no_desc = col.count_documents(MISSING_DESCRIPTION)

    # Legacy docs from the old integer-keyed seeder: _id != nameKey.
    legacy = [
        d
        for d in col.find({}, {"nameKey": 1, "name": 1})
        if str(d.get("_id")) != d.get("nameKey")
    ]

    print(f"Total mechanics:                    {total}")
    print(f"Missing requiresComponentTypes:     {no_types}")
    print(f"Missing bggId (not linked):         {no_bgg}")
    print(f"Missing description:                {no_desc}")
    if legacy:
        print(f"\nLegacy non-slug-keyed docs ({len(legacy)}):")
        for d in legacy:
            print(
                f"  _id={d['_id']!r}  nameKey={d.get('nameKey')!r}  "
                f"name={d.get('name')!r}"
            )


# --------------------------------------------------------------------------- #


def main() -> None:
    logging.basicConfig(level=logging.INFO, format="%(levelname)s %(message)s")
    p = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("seed", help="Upsert MECHANIC docs from SEED_MECHANICS")
    s.add_argument("--dry-run", action="store_true")
    s.set_defaults(fn=cmd_seed)

    d = sub.add_parser("draft", help="LLM-classify component types into a review file")
    d.add_argument("--out", default="mechanic_drafts.json")
    d.add_argument("--batch-size", type=int, default=10)
    d.add_argument("--limit", type=int, default=0, help="Max stubs this run (0 = all)")
    d.add_argument("--sleep", type=float, default=4.0, help="Seconds between LLM calls")
    d.set_defaults(fn=cmd_draft)

    a = sub.add_parser("apply", help="Write approved classifications into Mongo")
    a.add_argument("--file", default="mechanic_drafts.json")
    a.add_argument(
        "--approve-all", action="store_true", help="Ignore the approved flag"
    )
    a.add_argument("--dry-run", action="store_true")
    a.set_defaults(fn=cmd_apply)

    r = sub.add_parser("report", help="Show incomplete mechanics")
    r.set_defaults(fn=cmd_report)

    args = p.parse_args()
    args.fn(args)


if __name__ == "__main__":
    main()
