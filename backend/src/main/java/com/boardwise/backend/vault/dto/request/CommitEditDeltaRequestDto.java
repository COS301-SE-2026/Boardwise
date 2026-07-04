package com.boardwise.backend.vault.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommitEditDeltaRequestDto {
    private int expectedVersion;
    private String chunkId;
    private String deltaContent;
}
