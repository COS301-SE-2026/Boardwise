package com.boardwise.backend.vault.model;

import org.bson.types.ObjectId;
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
    private ObjectId chunkId;

    private Integer index;
    private String content;
}
