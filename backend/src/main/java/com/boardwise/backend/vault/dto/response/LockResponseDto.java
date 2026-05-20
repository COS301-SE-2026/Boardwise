package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LockResponseDto {
    private boolean lockGranted;
    private String lockedBy;
    private Instant expiresAt;
    private int currentVersion;
}