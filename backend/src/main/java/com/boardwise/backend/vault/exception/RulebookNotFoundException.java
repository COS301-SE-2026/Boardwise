package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class RulebookNotFoundException extends RuntimeException {
    public RulebookNotFoundException(ObjectId id) {
        super("Rulebook not found: " + id);
    }

    public RulebookNotFoundException(String message) { // Overload
        super(message);
    }
}