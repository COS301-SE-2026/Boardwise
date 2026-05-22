package com.boardwise.backend.vault.dto.request;

import lombok.Data;

@Data
public class CommitDeltaRequestDto {
    private int expectedVersion;
    private String delta;
}