package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class NoActionsToRedoException extends RuntimeException {
    public NoActionsToRedoException(ObjectId id) {
        super("No action to undo for rulebook: " + id);
    }

    public NoActionsToRedoException(String message) {
        super(message);
    }
}
