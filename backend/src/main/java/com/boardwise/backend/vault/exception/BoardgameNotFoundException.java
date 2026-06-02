package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class BoardgameNotFoundException extends RuntimeException {
    public BoardgameNotFoundException(ObjectId id) {
        super("Boardgame not found: " + id);
    }

    public BoardgameNotFoundException(String message) { // Overload
        super(message);
    }
}