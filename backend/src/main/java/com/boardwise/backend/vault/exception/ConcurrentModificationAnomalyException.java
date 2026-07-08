package com.boardwise.backend.vault.exception;

public class ConcurrentModificationAnomalyException extends RuntimeException{
    public ConcurrentModificationAnomalyException(String message){
        super(message);
    }
}