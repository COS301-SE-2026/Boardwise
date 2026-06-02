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

    @Field("rulebookId")
    private ObjectId rulebookId;

    @Field("editorId")
    private ObjectId editorId;

    @Field("delta")
    private String delta;

    @Field("versionAfter")
    private int versionAfter;

    @Field("committedAt")
    private Instant committedAt;
}
