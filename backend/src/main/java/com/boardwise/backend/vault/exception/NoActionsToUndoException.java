package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class NoActionsToUndoException extends RuntimeException {
    public NoActionsToUndoException(ObjectId id){
        super("No action to undo for rulebook: " + id);
    }

    public NoActionsToUndoException(String message){
        super(message);
    }
}
