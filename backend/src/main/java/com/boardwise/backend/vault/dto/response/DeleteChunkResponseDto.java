package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteChunkResponseDto {
    private boolean deleted;
    private int newVersion;
    private String chunkId;
    private Instant deletedAt;
}
