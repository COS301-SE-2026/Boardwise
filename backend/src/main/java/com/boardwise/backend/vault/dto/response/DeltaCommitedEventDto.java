package com.boardwise.backend.vault.dto.response;

import org.bson.types.ObjectId;

public record DeltaCommitedEventDto(
    ObjectId rulebookId,
    String chunkId,
    String deltaContent,
    int newVersion
){}
