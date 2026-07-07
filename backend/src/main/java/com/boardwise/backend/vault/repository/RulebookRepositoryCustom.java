package com.boardwise.backend.vault.repository;

import java.time.Instant;

import org.bson.types.ObjectId;

import com.boardwise.backend.vault.model.Rulebook;

public interface RulebookRepositoryCustom {
    Rulebook atomicAcquireWriteLock(ObjectId rulebookId, ObjectId userId, Instant newExpiry);

    Rulebook atomicValidateAndExtendLock(ObjectId rulebookId, ObjectId userId, long expectedVersion, Instant newExpiry);

    Rulebook atomicReleaseWriteLock(ObjectId rulebookId, ObjectId userId);

    void atomicReleaseAllWriteLocks(ObjectId userId);

    Long atomicPopUndoAndPushRedo(ObjectId rulebookId, ObjectId userId);
    
    Long atomicPopRedoAndPushUndo(ObjectId rulebookId, ObjectId userId);

    void atomicCommitForwardEdit(ObjectId rulebookId, Long newVersion);
}