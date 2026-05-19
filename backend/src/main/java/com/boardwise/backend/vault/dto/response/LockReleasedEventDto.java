package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LockReleasedEventDto {
    private String rulebookId;
    private String releasedBy;
    private String reason; // "voluntary" | "expired"
    private Instant releasedAt;
}