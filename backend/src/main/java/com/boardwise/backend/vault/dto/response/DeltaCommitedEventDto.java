package com.boardwise.backend.vault.dto.response;

public record DeltaCommitedEventDto(
    String rulebookId,
    String chunkId,
    String deltaContent,
    int newVersion
){}
