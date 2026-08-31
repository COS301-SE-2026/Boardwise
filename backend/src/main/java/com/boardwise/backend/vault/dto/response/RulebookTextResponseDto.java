package com.boardwise.backend.vault.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RulebookTextResponseDto {
    private String rulebookId;
    private List<ChunkDto> chunks;
    private long version;
    private String lockHeldBy;
    private Instant updatedAt;
}
