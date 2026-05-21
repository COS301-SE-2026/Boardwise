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

    @Field("rulebook_id")
    private ObjectId rulebookId;

    @Field("stage")
    private String stage;           // Sanitise | Extract

    @Field("job_status")
    private String jobStatus;       // Processing | Ready | PendingReview

    @Field("failure_reason")
    private String failureReason;

    @Field("started_at")
    private Instant startedAt;

    @Field("completed_at")
    private Instant completedAt;
}
