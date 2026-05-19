package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class LockNotHeldException extends RuntimeException {
    public LockNotHeldException(ObjectId userId) {
        super("User does not hold the write lock: " + userId);
    }

    public LockNotHeldException(String message) {
        super(message);
    }
}