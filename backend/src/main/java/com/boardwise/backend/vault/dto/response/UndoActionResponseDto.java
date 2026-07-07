package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UndoActionResponseDto {
    private boolean undone;
    private long newVersion;
    private String chunkId;
    private Instant undoneAt;
    
}