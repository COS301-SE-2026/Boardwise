package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boardwise.backend.vault.enums.EditType;

import lombok.*;

@Document(collection = "EDIT_EVENT")
@CompoundIndex(name = "rulebook_version_idx", def = "{'rulebookId': 1, 'versionAfter': -1}") // Index for finding all edits for a specific rulebook
@CompoundIndex(name = "rulebook_chunk_idx", def = "{'rulebookId': 1, 'chunkId': 1, 'versionAfter': -1}") // Index for finding the history of a specific chunk
@Getter // For immutability
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

    @Field("chunkId")
    private ObjectId chunkId;

    @Field("editType")
    private EditType editType;

    @Field("previousContent")
    private String previousContent;

    @Field("newContent")
    private String newContent;

    @Field("versionAfter")
    private int versionAfter;

    @Field("committedAt")
    private Instant committedAt;
}
