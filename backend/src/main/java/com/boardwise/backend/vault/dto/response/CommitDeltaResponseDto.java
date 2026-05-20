package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommitDeltaResponseDto {
    private boolean committed;
    private int newVersion;
    private Instant committedAt;
}