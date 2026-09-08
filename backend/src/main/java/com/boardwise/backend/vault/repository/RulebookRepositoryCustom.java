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

    ObjectId atomicPopUndoAndPushRedo(ObjectId rulebookId, ObjectId userId);
    
    ObjectId atomicPopRedoAndPushUndo(ObjectId rulebookId, ObjectId userId);

    void atomicCommitForwardEdit(ObjectId rulebookId, ObjectId eventId);
    
    Page<Rulebook> searchWithFilters(
        String search, String genre, List<String> languages,
        Integer playerCount, Integer duration, Integer minAge,
        Pageable pageable);
}