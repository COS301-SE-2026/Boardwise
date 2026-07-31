package com.boardwise.backend.vault.exception;

public class InvalidPaginationException extends RuntimeException {
    public InvalidPaginationException(String message){
        super(message);
    }
}
