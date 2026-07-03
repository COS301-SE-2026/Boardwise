package com.boardwise.backend.vault.dto.request;

import org.bson.types.ObjectId;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommitEditDeltaRequestDto {
    private int expectedVersion;
    private ObjectId chunkId;
    private String deltaContent;
}
