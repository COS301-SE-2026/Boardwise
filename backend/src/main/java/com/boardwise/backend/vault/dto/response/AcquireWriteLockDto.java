package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcquireWriteLockDto {
    private boolean lockGranted;
    private String lockedBy; // Username instead of id
    private Instant expiresAt;
    private int currentVersion;
}
