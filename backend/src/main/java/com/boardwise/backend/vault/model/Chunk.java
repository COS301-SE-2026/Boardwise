package com.boardwise.backend.vault.model;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Chunk {
    @JsonSerialize(using = ToStringSerializer.class)
    @Field("chunkId")
    private ObjectId chunkId;

    @Field("index")
    private Integer index;

    @Field("content")
    private String content;
}
