package com.boardwise.backend.vault.exception;

import org.bson.types.ObjectId;

public class ChunkNotFoundException extends RuntimeException {
    public ChunkNotFoundException(ObjectId rulebookId, ObjectId chunkId){
        super("Chunk "+ chunkId +" not found in rulebook " + rulebookId);
    }

    public ChunkNotFoundException(String message){
        super(message);
    }
}
