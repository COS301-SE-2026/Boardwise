package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsertNewChunkResponseDto {
    private boolean inserted;
    private int newVersion;
    private String chunkId;
    private int actualIndex;
    private Instant insertedAt;
}
