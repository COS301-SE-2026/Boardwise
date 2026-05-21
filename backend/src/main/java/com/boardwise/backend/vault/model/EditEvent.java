package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

@Document(collection = "EDIT_EVENT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditEvent {
    @Id
    private ObjectId id;

    @Field("rulebook_id")
    private ObjectId rulebookId;

    @Field("editor_id")
    private ObjectId editorId;

    @Field("delta")
    private String delta;

    @Field("version_after")
    private int versionAfter;

    @Field("committed_at")
    private Instant committedAt;
}
