package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "INGESTION_JOB")
@Data
@Builder
public class IngestionJob {
    @Id
    private ObjectId id;
    private ObjectId rulebookId;
    private String stage;           // Sanitise | Extract
    private String jobStatus;       // Processing | Ready | PendingReview
    private String failureReason;
    private Instant startedAt;
    private Instant completedAt;
}
