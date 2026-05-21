package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditEventResponseDto {
    private String id;
    private String rulebookId;
    private String editorId;
    private String delta;
    private int versionAfter;
    private Instant committedAt;
}
