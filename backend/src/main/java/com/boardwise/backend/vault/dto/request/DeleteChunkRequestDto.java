package com.boardwise.backend.vault.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteChunkRequestDto {
    private int expectedVersion;
    private String chunkId;
    private String chunkBeforeId; // The id of the chunk that came before the current chunk
    private String previousContent;
}
