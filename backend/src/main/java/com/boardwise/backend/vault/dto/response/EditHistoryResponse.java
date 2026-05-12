package com.boardwise.backend.vault.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditHistoryResponse {
    private String rulebookId;
    private int totalEdits;
    private List<EditEventResponseDto> edits;
}
