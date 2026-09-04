package com.boardwise.backend.vault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkDto {
    private String chunkId;
    private int index;
    private String content;
}
