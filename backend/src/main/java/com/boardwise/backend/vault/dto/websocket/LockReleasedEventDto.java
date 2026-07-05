package com.boardwise.backend.vault.dto.websocket;

import java.time.Instant;

public record LockReleasedEventDto(
    String rulebookId,
    String releasedByUserId,
    String releasedByUsername,
    String reason, // voluntary | expired | disconnected
    Instant releasedAt
){}
