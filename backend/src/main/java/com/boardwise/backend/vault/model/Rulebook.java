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

    @Field("gameId")
    private ObjectId gameId;

    @Field("title")
    private String title;

    @Field("edition")
    private String edition;

    @Field("status")
    private String status; // Processing | Ready | PendingReview | Failed

    @Field("version")
    private int version;

    @Field("contributorId")
    private ObjectId contributorId;

    @Field("contributorUsername")
    private String contributorUsername;

    @Field("description")
    private String description;

    @Field("language")
    private String language;

    @Field("r2PdfKey")
    private String r2PdfKey;

    @Field("uploadedAt")
    private Instant uploadedAt;

    @Field("updatedAt")
    private Instant updatedAt;
}
