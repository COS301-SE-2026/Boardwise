package com.boardwise.backend.vault.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseColabResponseDto {
    private boolean done;
    private long newVersion;
    private String chunkId;
    private Instant doneAt;
    private Instant lockExpiresAt;
}