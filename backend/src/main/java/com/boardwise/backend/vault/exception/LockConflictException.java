package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class LockConflictException extends RuntimeException {
    public LockConflictException(ObjectId rulebookId) {
        super("Write lock already held on rulebook: " + rulebookId);
    }

    public LockConflictException(String message) { // Overload for situations where ObjectId is not available
        super(message);
    }
}