package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

@Document(collection = "RULEBOOK")
@Data
@Builder
public class Rulebook {
    @Id
    private ObjectId id;
    private String gameName;
    private String edition;
    private String status; // Processing | Ready | PendingReview
    private int version;
    private ObjectId contributorId;
    private String r2PdfKey;
    private Instant uploadedAt;
    private Instant updatedAt;

}
