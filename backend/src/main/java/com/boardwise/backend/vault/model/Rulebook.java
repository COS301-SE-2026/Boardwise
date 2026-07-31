package com.boardwise.backend.vault.model;

import java.time.Instant;
import java.util.List;

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

    @Field("coverUrl")
    private String coverUrl;

    @Field("gameId")
    private ObjectId gameId;

    @Field("title")
    private String title;

    @Field("edition")
    private String edition;

    @Field("status")
    private String status; // Processing | Ready | PendingReview | Failed

    @Field("version")
    private long version;

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

    @Field("r2CoverKey")
    private String r2CoverKey;

    @Field("lockHeldBy")
    private ObjectId lockHeldBy;

    @Field("lockExpiresAt")
    private Instant lockExpiresAt;

    @Field("undoStack")
    private List<Long> undoStack;

    @Field("redoStack")
    private List<Long> redoStack;

    @Field("uploadedAt")
    private Instant uploadedAt;

    @Field("updatedAt")
    private Instant updatedAt;
    
    @Field("genres")
    private List<String> genres;

    @Field("minPlayers")
    private Integer minPlayers;

    @Field("maxPlayers")
    private Integer maxPlayers;

    @Field("duration")
    private Integer duration;

    @Field("minAge")
    private Integer minAge;
}
