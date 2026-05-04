package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "RULEBOOK_TEXT")
@Data
@Builder
public class RulebookText {
    @Id
    private ObjectId id;
    private ObjectId rulebookId;
    private String content;
    private int version;
    private Instant updatedAt;
}
