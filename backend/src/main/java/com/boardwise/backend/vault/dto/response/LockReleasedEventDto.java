package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

public record LockReleasedEventDto(
    String rulebookId,
    String releasedByUserId,
    String releasedByUsername,
    String reason, // voluntary | expired | disconnected
    Instant releasedAt
){}
