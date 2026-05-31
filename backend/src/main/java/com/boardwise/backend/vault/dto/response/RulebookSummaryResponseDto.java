package com.boardwise.backend.vault.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RulebookSummaryResponseDto {
    private String id;
    private String gameName;
    private String edition;
    private int version;
}
