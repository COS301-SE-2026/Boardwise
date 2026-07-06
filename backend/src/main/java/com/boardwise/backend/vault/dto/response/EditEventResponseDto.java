package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EditEventResponseDto {
    private String id;
    private String rulebookId;
    private String editor;
    private String chunkId;
    private String editType;
    private String previousContent;
    private String newContent;
    private long versionAfter;
    private Instant committedAt;
}
