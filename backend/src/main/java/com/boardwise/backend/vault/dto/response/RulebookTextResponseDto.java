package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RulebookTextResponseDto {
    private String rulebookId;
    private String content;
    private int version;
    private String lockHeldBy;
    private Instant updatedAt;
}
