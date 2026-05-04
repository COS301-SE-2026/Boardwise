package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "EDIT_EVENT")
@Data
@Builder
public class EditEvent {
    @Id
    private ObjectId id;
    private ObjectId rulebookId;
    private ObjectId editorId;
    private String delta;
    private int versionAfter;
    private Instant committedAt;
}
