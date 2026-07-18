package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommitEditDeltaResponseDto {
    private boolean committed;
    private long newVersion;
    private Instant committedAt;
    private Instant lockExpiresAt;
}
