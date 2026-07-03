package com.boardwise.backend.vault.repository;

import org.bson.types.ObjectId;

public interface RulebookTextRepositoryCustom {
    void atomicUpdateChunk(ObjectId rulebookId, ObjectId chunkId, String newContent);
}
