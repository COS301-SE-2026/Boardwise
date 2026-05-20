db = db.getSiblingDB('boardwise');

// Clear existing data
db.RULEBOOK.drop();
db.RULEBOOK_TEXT.drop();
db.WRITE_LOCK.drop();
db.EDIT_EVENT.drop();
db.INGESTION_JOB.drop();

// Insert rulebooks
const contributorId = new ObjectId();
const rulebookId1 = new ObjectId();
const rulebookId2 = new ObjectId();
const editorId = new ObjectId();

db.RULEBOOK.insertMany([
    {
        _id: rulebookId1,
        game_name: "Catan",
        edition: "3rd Edition",
        status: "Ready",
        version: 3,
        contributor_id: contributorId,
        r2_pdf_key: "rulebooks/" + rulebookId1.toHexString() + "/catan.pdf",
        uploaded_at: new Date(),
        updated_at: new Date()
    },
    {
        _id: rulebookId2,
        game_name: "Ticket to Ride",
        edition: "1st Edition",
        status: "Ready",
        version: 1,
        contributor_id: contributorId,
        r2_pdf_key: "rulebooks/" + rulebookId2.toHexString() + "/ticket-to-ride.pdf",
        uploaded_at: new Date(),
        updated_at: new Date()
    },
    {
        _id: new ObjectId(),
        game_name: "Wingspan",
        edition: "2nd Edition",
        status: "Processing",
        version: 0,
        contributor_id: contributorId,
        r2_pdf_key: null,
        uploaded_at: new Date(),
        updated_at: new Date()
    }
]);

// Insert rulebook text
db.RULEBOOK_TEXT.insertMany([
    {
        _id: new ObjectId(),
        rulebook_id: rulebookId1,
        content: "Catan is a multiplayer board game. Players collect resources and build roads, settlements and cities.",
        version: 3,
        updated_at: new Date()
    },
    {
        _id: new ObjectId(),
        rulebook_id: rulebookId2,
        content: "Ticket to Ride is a cross-country train adventure game.",
        version: 1,
        updated_at: new Date()
    }
]);

// Insert a write lock on rulebookId1
db.WRITE_LOCK.insertOne({
    _id: new ObjectId(),
    rulebook_id: rulebookId1,
    held_by_user_id: editorId,
    acquired_at: new Date(),
    expires_at: new Date(Date.now() + 30000)
});

// Insert edit events for rulebookId1
db.EDIT_EVENT.insertMany([
    {
        _id: new ObjectId(),
        rulebook_id: rulebookId1,
        editor_id: editorId,
        delta: "Added setup instructions.",
        version_after: 2,
        committed_at: new Date(Date.now() - 60000)
    },
    {
        _id: new ObjectId(),
        rulebook_id: rulebookId1,
        editor_id: editorId,
        delta: "Corrected victory point rules.",
        version_after: 3,
        committed_at: new Date()
    }
]);

// Insert ingestion jobs
db.INGESTION_JOB.insertMany([
    {
        _id: new ObjectId(),
        rulebook_id: rulebookId1,
        stage: "Extract",
        job_status: "Ready",
        failure_reason: null,
        started_at: new Date(Date.now() - 120000),
        completed_at: new Date(Date.now() - 60000)
    },
    {
        _id: new ObjectId(),
        rulebook_id: rulebookId2,
        stage: "Extract",
        job_status: "Ready",
        failure_reason: null,
        started_at: new Date(Date.now() - 120000),
        completed_at: new Date(Date.now() - 60000)
    }
]);

print("Seed complete.");
print("Contributor ID: " + contributorId.toHexString());
print("Rulebook 1 ID (Catan): " + rulebookId1.toHexString());
print("Rulebook 2 ID (Ticket to Ride): " + rulebookId2.toHexString());
print("Editor ID: " + editorId.toHexString());