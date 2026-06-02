package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

@Document(collection = "INGESTION_JOB")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionJob {

    @Id
    private ObjectId id;

    @Field("rulebookId")
    private ObjectId rulebookId;

    @Field("stage")
    private String stage; // Sanitise | Extract | Chunk | Vectorise

    @Field("jobStatus")
    private String jobStatus; // Processing | Completed | Failed

    @Field("failureReason")
    private String failureReason;

    @Field("startedAt")
    private Instant startedAt;

    @Field("completedAt")
    private Instant completedAt; // null while job is still in progress
}
