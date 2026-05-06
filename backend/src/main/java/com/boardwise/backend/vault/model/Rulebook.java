package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

@Document(collection = "RULEBOOK")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rulebook {

    @Id
    private ObjectId id;

    @Field("game_name")
    private String gameName;

    @Field("edition")
    private String edition;

    @Field("status")
    private String status; // Processing | Ready | PendingReview

    @Field("version")
    private int version;

    @Field("contributor_id")
    private ObjectId contributorId;

    @Field("r2_pdf_key")
    private String r2PdfKey;

    @Field("uploaded_at")
    private Instant uploadedAt;

    @Field("updated_at")
    private Instant updatedAt;
}
