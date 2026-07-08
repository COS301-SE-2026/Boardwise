package com.boardwise.backend.vault.dto.websocket;

import java.time.Instant;

public record LockAcquiredEventDto(
    String rulebookId,
    String lockedByUserId,
    String lockedByUsername,
    Instant expiresAt,
    long currentVersion
){}
