package com.boardwise.backend.vault.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeleteChunkRequestDto extends VaultBaseRequestDto {
    private String chunkId;
    private String chunkBeforeId; // The id of the chunk that came before the current chunk
}
