# Entity Relationship Diagram - Boardwise (Full System)

**Project:** Boardwise  
**Version:** 1.0  
**Sprint:** 1 - Demo 1 (MVP)  
**Date:** 2026-05-02  
**Status:** Active

---

## Conventions

- All `_id` fields are of type `ObjectId` and serve as the primary identifier for each document.
- Fields suffixed with `_id` (e.g. `userId`, `groupId`) are `ObjectId` references to documents in other collections.
- `Array_String` denotes an embedded array of scalar string values.
- `Array_ObjectId` denotes an embedded array of `ObjectId` references to documents in another collection.
- `String?` denotes an optional field that may be null or absent.
- `Decimal` denotes a numeric value stored as a decimal type for monetary precision.
- Relationships are shown as logical references, not foreign key constraints, consistent with MongoDB's document model.
- Field naming follows `camelCase` consistently across all collections.
- Collections are colour-grouped by service boundary:
  - **User Service** - USER, PREFERENCES, FRIEND_REQUEST, FRIENDSHIP, GROUP, GROUP_MEMBERSHIP, EVENT, RSVP
  - **Shared** - BOARD_GAME (referenced by both User Service and Marketplace Service)
  - **Marketplace Service** - LISTING, RETAIL_SOURCE
  - **Shared Library (The Vault)** - RULEBOOK, RULEBOOK_TEXT, WRITE_LOCK, EDIT_EVENT, INGESTION_JOB

---

## Cross-Service Notes

- `BOARD_GAME` is a **shared collection** owned by the User Service but referenced by the Marketplace Service (via `gameId` on `LISTING` and `RETAIL_SOURCE`). Cross-service reads are mediated through the BFF and REST API calls - the Marketplace Service does not query the User Service database directly.
- `USER` is owned exclusively by the User Service. The Marketplace Service stores only `userId` as an `ObjectId` reference on `LISTING`. The Vault stores only `_id`, `username`, and `display_name` as a minimal cross-service stub for display purposes.
- All cross-service `ObjectId` references are logical references only - MongoDB does not enforce referential integrity across collections or databases.

---

## Diagram

```mermaid
erDiagram

    USER {
        ObjectId _id
        String username
        String emailAddress
        String password
        String profilePicture
        String location
        Array_ObjectId ownedGames
        Object preferences
        Date createdAt
    }

    PREFERENCES {
        String visibility
        Array_String genres
    }

    FRIEND_REQUEST {
        ObjectId _id
        ObjectId senderId
        ObjectId receiverId
        Date createdAt
    }

    FRIENDSHIP {
        ObjectId _id
        ObjectId userAId
        ObjectId userBId
        Date createdAt
    }

    GROUP {
        ObjectId _id
        String name
        String description
        String visibility
        String category
        ObjectId ownerId
        Date createdAt
    }

    GROUP_MEMBERSHIP {
        ObjectId _id
        ObjectId userId
        ObjectId groupId
        Date joinedAt
    }

    EVENT {
        ObjectId _id
        String name
        Date date
        String time
        String location
        String visibility
        ObjectId creatorId
        Array_ObjectId gameId
        Date createdAt
    }

    RSVP {
        ObjectId _id
        ObjectId userId
        ObjectId eventId
        String status
        Date respondedAt
    }
    
    TOKEN_BLACKLIST {
	    ObjectId _id
	    String jti
	    Date createdAt
	    Date expiresAt
    }

    BOARD_GAME {
        ObjectId _id
        String title
        String edition
        String description
        String imageUrl
        Array_String genre
    }

    LISTING {
        ObjectId _id
        ObjectId userId
        ObjectId gameId
        String location
        String gameTitle
        String itemType
        String listingType
        Decimal price
        String description
        String imageUrl
        String status
        Date createdAt
        Date updatedAt
    }

    RETAIL_SOURCE {
        ObjectId _id
        ObjectId gameId
        String retailerName
        String storeUrl
    }

    RULEBOOK {
	    ObjectId _id
	    ObjectId gameId
	    String gameName
	    String edition
	    String status
	    int version
	    ObjectId contributorId
	    String r2PdfKey
	    Date uploadedAt
	    Date updatedAt
    }

    RULEBOOK_TEXT {
	    ObjectId _id
	    ObjectId rulebookId
	    int version
	    Date updatedAt
	    Array_Object chunks
    }
    
    CHUNK {
	    ObjectId _id
	    int index
	    String content
    }

    WRITE_LOCK {
        ObjectId _id
        ObjectId rulebookId
        ObjectId heldByUserId
        Date acquiredAt
        Date expiresAt
    }

    EDIT_EVENT {
        ObjectId _id
        ObjectId rulebookId
        ObjectId editorId
        String delta
        int versionAfter
        Date committedAt
    }

    INGESTION_JOB {
        ObjectId _id
        ObjectId rulebookId
        String stage
        String jobStatus
        String failureReason
        Date startedAt
        Date completedAt
    }

    USER ||--|| PREFERENCES : "embeds"
    USER }o--o{ BOARD_GAME : "owns (ownedGames[])"
    USER ||--o{ FRIEND_REQUEST : "sends (senderId)"
    USER ||--o{ FRIEND_REQUEST : "receives (receiverId)"
    USER ||--o{ FRIENDSHIP : "connected as (user_a_id)"
    USER ||--o{ FRIENDSHIP : "connected as (user_b_id)"
    USER ||--o{ GROUP_MEMBERSHIP : "joins (userId)"
    USER ||--|| GROUP : "owns (ownerId)"
    USER ||--o{ EVENT : "creates (creatorId)"
    USER ||--o{ RSVP : "responds (userId)"
    GROUP ||--o{ GROUP_MEMBERSHIP : "has (groupId)"
    EVENT ||--o{ RSVP : "has (eventId)"
    EVENT ||--o| BOARD_GAME : "references (gameId)"

    BOARD_GAME ||--o{ LISTING : "has listings (gameId)"
    BOARD_GAME ||--o{ RETAIL_SOURCE : "has retail sources (gameId)"
    USER ||--o{ LISTING : "creates (userId)"

    USER ||--o{ RULEBOOK : "contributes (contributorId)"
    USER ||--o{ WRITE_LOCK : "holds (held_by_userId)"
    USER ||--o{ EDIT_EVENT : "edits (editorId)"
    RULEBOOK ||--|| RULEBOOK_TEXT : "has text (rulebookId)"
    RULEBOOK ||--o| WRITE_LOCK : "guarded by (rulebookId)"
    RULEBOOK ||--o{ EDIT_EVENT : "tracks via (rulebookId)"
    RULEBOOK ||--|| INGESTION_JOB : "processed by (rulebookId)"
```

---

## Collection Descriptions

### USER SERVICE

#### USER
The primary collection for the User Service and the central entity of the entire system. Stores all account credentials, profile information, and embedded preferences. The `ownedGames` field is an array of `ObjectId` references to documents in the `BOARD_GAME` collection, representing the user's game inventory. Preferences are embedded directly on the User document as they are always accessed alongside the user and never queried independently. Referenced by all three services via `ObjectId`.

#### PREFERENCES *(Embedded in USER)*
Not a standalone collection. Preferences are embedded as a subdocument within each `USER` document. Contains arrays of genre and mechanic strings representing the user's board game interests.

#### FRIEND_REQUEST
Tracks pending friend connection requests between users. Each document references a `senderId` and `receiverId` (both `ObjectId` references to `USER`). A `FRIEND_REQUEST` document has no status field - it exists only while the request is pending. On acceptance, the document is deleted and a `FRIENDSHIP` document is created. On rejection, the document is simply deleted with no trace retained.

#### FRIENDSHIP
Represents an established mutual connection between two users. Created when a `FRIEND_REQUEST` is accepted. References `userAId` and `userBId` (both `ObjectId` references to `USER`). Queried directly for friends list lookups, making friend retrieval efficient without filtering by status. When a user unfriends another, the `FRIENDSHIP` document is permanently deleted.

#### GROUP
Stores user-created social groups. References the `ownerId` (the creating user) as an `ObjectId`. Group membership is tracked separately in the `GROUP_MEMBERSHIP` collection.

#### GROUP_MEMBERSHIP
Join collection linking `USER` and `GROUP`. Each document holds a `userId` and `groupId` reference along with a `joinedAt` timestamp. This collection is queried to determine group members and a user's group count.

#### EVENT
Stores gaming events created by users. References the `creatorId` (the organising user) and `gameId` (the board game being played). The `visibility` field holds one of: `Public`, `Private`. RSVPs are tracked separately in the `RSVP` collection.

#### RSVP
Tracks user attendance responses to events. Each document references a `userId` and `eventId`. The `status` field holds one of: `Joined`, `Declined`.

---

### SHARED

#### BOARD_GAME
Stores the board game catalogue entries shared across the system. Owned by the User Service but referenced by the Marketplace Service (via `gameId` on `LISTING` and `RETAIL_SOURCE`). Referenced by `USER` (via `ownedGames`), `EVENT` (via `gameId`), `LISTING` (via `gameId`), and `RETAIL_SOURCE` (via `gameId`). Genre and mechanics are embedded as arrays of strings since they are simple scalar values that do not require independent querying. Cross-service reads are mediated through the BFF and REST APIs - the Marketplace Service does not query the User Service database directly.

---

### MARKETPLACE SERVICE

#### LISTING
The primary collection for the Marketplace Service. Stores all peer-to-peer rental and sale listings created by authenticated users. The `userId` field is an `ObjectId` reference to the `USER` document in the User Service - it is not embedded, as the User Service owns that domain. The `gameTitle` field is denormalised directly onto the `LISTING` document to allow efficient browse and filter queries without requiring a cross-service lookup to `BOARD_GAME` on every request.

The `itemType` field holds one of: `BOARD_GAME`, `MERCHANDISE`, `EXPANSION`.
The `listingType` field holds one of: `RENT`, `SALE`.
The `status` field holds one of: `ACTIVE`, `INACTIVE`, `DELETED`. `INACTIVE` represents a soft delete (mark as unavailable) that can be reversed. `DELETED` represents a permanent removal from public view.

#### RETAIL_SOURCE
Stores aggregated external retail purchase links for board game titles. Each document references a `gameId` linking it to a `BOARD_GAME` document. Retail sources are populated by the backend retail aggregation service and are never user-generated. The `linkType` field holds one of: `ONLINE`, `IN_STORE`. The `inStockIndication` fields are best-effort values and are not guaranteed to be real-time accurate.

---

### SHARED LIBRARY - THE VAULT

#### RULEBOOK
The aggregate root for The Vault. Stores rulebook metadata including the Cloudflare R2 key for the raw PDF (`r2PdfKey`). The `gameId` field is an `ObjectId` reference to the `BOARD_GAME` collection, identifying which board game this rulebook belongs to. The `gameName` field is denormalised directly onto the document to allow Vault list and search queries to display the game name without a cross-service lookup. The `status` field holds one of: `Processing`, `Ready`, `PendingReview`, `Failed`. `Processing` is set when ingestion begins. `Ready` is set on successful pipeline completion. `PendingReview` is set when a rulebook requires moderation or has been flagged by a collaborator as needing review before it is broadly visible. `Failed` is set when the ingestion pipeline encounters an unrecoverable error - the corresponding `INGESTION_JOB` will carry the `failureReason`. The `version` field is incremented on every accepted collaborative edit. Owns `RULEBOOK_TEXT`, `WRITE_LOCK`, `EDIT_EVENT`, and `INGESTION_JOB` via one-to-one or one-to-many relationships.

#### RULEBOOK_TEXT
Stores the mutable collaborative text content of a rulebook as an array of embedded `CHUNK` subdocuments. Separated from `RULEBOOK` intentionally - the chunk array can be large, and keeping it in a separate collection ensures that list and search queries on `RULEBOOK` remain lean and never load the full text unnecessarily.

Each chunk carries a `chunkId`, an `index` (its ordinal position in the document), and a `content` string (the text of that segment). Chunks are the natural unit of both display and editing - a collaborative delta (AC-VLT-07) targets a specific `chunkId` rather than a byte offset in an unbounded string, making delta semantics unambiguous and WebSocket broadcast payloads lightweight.

Vector embeddings produced by the `Vectorise` stage of the ingestion pipeline are not stored here. They are stored separately in a vector store and keyed by `chunkId`. This keeps `RULEBOOK_TEXT` reads fast and avoids transmitting large float arrays to the collaborative editor, which has no use for them.

#### CHUNK *(Embedded in RULEBOOK_TEXT)*
Not a standalone collection. Chunks are embedded as subdocuments within the `chunks` array of each `RULEBOOK_TEXT` document. Each chunk represents a discrete segment of the rulebook's extracted text, carrying a stable `chunkId`, an `index` denoting its position in the document, and a `content` string. The `chunkId` is the key used by the collaborative delta mechanism (AC-VLT-07) to target specific segments during editing, and by the vector store to associate each embedding with its source text.
Represents the MRSW exclusive write lock held on a rulebook during collaborative editing. A `RULEBOOK` either has a lock (`0..1` relationship) or it does not. The `expiresAt` field drives the 30-second idle expiry - Spring Boot enforces this on every lock-check request.

#### EDIT_EVENT
The immutable event sourcing ledger for collaborative edits. Each document stores a delta (the change applied) and the `versionAfter` (the resulting version number). This enables full edit history reconstruction and rollback without joining back to `RULEBOOK_TEXT`.

#### INGESTION_JOB
Tracks the state of the FastAPI pipe-and-filter ingestion pipeline for each uploaded rulebook. The `stage` field reflects the current or last completed pipeline stage: `Sanitise`, `Extract`, `Chunk`, `Vectorise`. The `jobStatus` field holds one of: `Processing`, `Completed`, `Failed`. `Processing` indicates the pipeline is actively running. `Completed` indicates all stages finished successfully, at which point the parent `RULEBOOK` status transitions to `Ready`. `Failed` indicates an unrecoverable error occurred at the stage recorded in `stage` - the `failureReason` field captures the error detail and the parent `RULEBOOK` status transitions to `Failed`. The `completedAt` field is null while the job is still in progress.
