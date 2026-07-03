package com.boardwise.backend.vault.repository;

import java.time.Instant;

import org.bson.types.ObjectId;

import com.boardwise.backend.vault.model.Rulebook;

public interface RulebookRepositoryCustom {
    Rulebook atomicAcquireWriteLock(ObjectId rulebookId, ObjectId userId, Instant newExpiry);

    Rulebook atomicValidateAndExtendLock(ObjectId rulebookId, ObjectId userId, int expectedVersion, Instant newExpiry);
}