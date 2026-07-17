package com.boardwise.backend.vault.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RulebookSummaryResponseDto {
    private String id;
    private String coverUrl;
    private String title;
    private String language;
    private String edition;
    private long version;
    private List<String> genres;
    private Integer minPlayers;
    private Integer maxPlayers;
}
