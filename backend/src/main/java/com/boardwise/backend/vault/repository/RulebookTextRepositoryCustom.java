package com.boardwise.backend.vault.repository;

import org.bson.types.ObjectId;

import com.boardwise.backend.vault.model.RulebookText;

public interface RulebookTextRepositoryCustom {
    void atomicUpdateChunk(ObjectId rulebookId, ObjectId chunkId, String newContent);

    RulebookText atomicInsertChunk(ObjectId rulebookId, String content, int insertIndex);

    boolean atomicDeleteChunk(ObjectId rulebookId, ObjectId chunkId);
}
