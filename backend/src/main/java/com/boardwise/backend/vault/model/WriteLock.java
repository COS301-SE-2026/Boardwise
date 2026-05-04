package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "WRITE_LOCK")
@Data
@Builder
public class WriteLock {
    @Id
    private ObjectId id;
    private ObjectId rulebookId;
    private ObjectId heldByUserId;
    private Instant acquiredAt;
    private Instant expiresAt;      // used for 30-second idle expiry
}
