package com.boardwise.backend.vault.repository;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.boardwise.backend.vault.model.Rulebook;

public interface RulebookRepositoryCustom {
    Rulebook atomicAcquireWriteLock(ObjectId rulebookId, ObjectId userId, Instant newExpiry);

    Rulebook atomicValidateAndExtendLock(ObjectId rulebookId, ObjectId userId, long expectedVersion, Instant newExpiry);

    Rulebook atomicReleaseWriteLock(ObjectId rulebookId, ObjectId userId);

    void atomicReleaseAllWriteLocks(ObjectId userId);

    Long atomicPopUndoAndPushRedo(ObjectId rulebookId, ObjectId userId);
    
    Long atomicPopRedoAndPushUndo(ObjectId rulebookId, ObjectId userId);

    void atomicCommitForwardEdit(ObjectId rulebookId, Long newVersion);
    
    Page<Rulebook> searchWithFilters(
        String search, String genre, List<String> languages,
        Integer playerCount, Integer duration, Integer minAge,
        Pageable pageable);
}