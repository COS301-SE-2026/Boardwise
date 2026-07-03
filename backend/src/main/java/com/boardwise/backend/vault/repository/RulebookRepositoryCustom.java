package com.boardwise.backend.vault.repository;

import java.time.Instant;

import com.boardwise.backend.vault.model.Rulebook;

public interface RulebookRepositoryCustom {
    Rulebook atomicAcquireWriteLock(String rulebookId, String userId, Instant newExpiry);
}