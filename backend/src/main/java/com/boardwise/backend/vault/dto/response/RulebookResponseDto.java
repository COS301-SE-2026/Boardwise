package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RulebookResponseDto {
    private String id;
    private String gameName;
    private String edition;
    private String status;
    private int version;
    private String contributorId;
    private String lockHeldBy;
    private Instant uploadedAt;
    private Instant updatedAt;
}
