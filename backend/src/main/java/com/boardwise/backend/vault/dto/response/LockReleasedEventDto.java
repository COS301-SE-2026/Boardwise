package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import org.bson.types.ObjectId;

public record LockReleasedEventDto(
    ObjectId rulebookId,
    ObjectId releasedBy,
    String reason, // voluntary | expired | disconnect
    Instant releasedAt
){}
