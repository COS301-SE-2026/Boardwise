package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

@Document(collection = "WRITE_LOCK")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WriteLock {

    @Id
    private ObjectId id;

    @Field("rulebook_id")
    private ObjectId rulebookId;

    @Field("held_by_user_id")
    private ObjectId heldByUserId;

    @Field("acquired_at")
    private Instant acquiredAt;

    @Field("expires_at")
    private Instant expiresAt;      // used for 30-second idle expiry
}
