package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

public record LockAcquiredEventDto(
    String rulebookId,
    String lockedByUserId,
    String lockedByUsername,
    Instant expiresAt,
    int currentVersion
){}
