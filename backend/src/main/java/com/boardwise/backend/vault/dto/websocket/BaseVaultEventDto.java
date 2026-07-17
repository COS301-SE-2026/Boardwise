package com.boardwise.backend.vault.dto.websocket;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder // Allows for builder inheritance
@NoArgsConstructor
@AllArgsConstructor
public class BaseVaultEventDto {
    private String eventType; // CHUNK_INSERTED | DELTA_COMMITTED | CHUNK_DELETED 
    private String rulebookId;
    private String editorId;
    private long version;
    private Instant timestamp;
    private String chunkId;
}
