package com.boardwise.backend.vault.dto.websocket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ChunkDeletedEventDto extends BaseVaultEventDto {
    private String chunkId;
}
