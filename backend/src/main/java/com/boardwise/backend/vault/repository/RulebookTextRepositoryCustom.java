package com.boardwise.backend.vault.repository;

import org.bson.types.ObjectId;

public interface RulebookTextRepositoryCustom {
    void atomicUpdateChunk(ObjectId rulebookId, ObjectId chunkId, String newContent);

    boolean atomicInsertChunk(ObjectId rulebookId, ObjectId chunkId, String content, int insertIndex, int lastIndex);
}
