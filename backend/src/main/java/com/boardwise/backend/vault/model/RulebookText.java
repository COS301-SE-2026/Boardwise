package com.boardwise.backend.vault.model;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

@Document(collection = "RULEBOOK_TEXT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RulebookText {

    @Id
    private ObjectId id;

    @Field("rulebook_id")
    private ObjectId rulebookId;

    @Field("game_id")
    private ObjectId gameId;

    @Field("content")
    private String content;

    @Field("version")
    private int version;

    @Field("updated_at")
    private Instant updatedAt;
}
