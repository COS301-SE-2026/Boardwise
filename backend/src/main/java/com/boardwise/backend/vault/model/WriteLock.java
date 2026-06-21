package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Field("rulebookId")
    @Indexed(unique = true)
    private ObjectId rulebookId;

    @Field("heldByUserId")
    private ObjectId heldByUserId;

    @Field("acquiredAt")
    private Instant acquiredAt;

    @Field("expiresAt")
    private Instant expiresAt;
}
