package com.boardwise.backend.vault.model;

import java.time.Instant;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Document(collection = "RULEBOOK_TEXT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RulebookText {

    @Id
    private ObjectId id;

    @Field("rulebookId")
    private ObjectId rulebookId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Field("chunkId")
    private ObjectId chunkId;

    @Field("index")
    private Integer index;

    @Field("content")
    private String content;

    @Field("embedding")
    private Double[] embedding;

    @Field("charCount")
    private int charCount;
    
    @Field("createdAt")
    private Instant createdAt;

    @Field("updatedAt")
    private Instant updatedAt;
}
