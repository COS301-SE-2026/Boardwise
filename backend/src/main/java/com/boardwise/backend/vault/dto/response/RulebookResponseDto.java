package com.boardwise.backend.vault.dto.response;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RulebookResponseDto {
    private String id;
    private String coverUrl;
    private String title;
    private String edition;
    private List<String> genres;
    private long version;
    private String status;
    private String contributorUsername;
    private String description;
    private String language;
    private String lockHeldBy;
    private Instant lockExpiresAt;
    private Instant uploadedAt;
    private Instant updatedAt;
    private Integer minPlayers;
    private Integer maxPlayers;
}
