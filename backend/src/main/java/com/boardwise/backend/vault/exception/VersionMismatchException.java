package com.boardwise.backend.vault.exception;

public class VersionMismatchException extends RuntimeException {
    public VersionMismatchException(long expected, long actual) {
        super("Version mismatch - expected: " + expected + ", actual: " + actual);
    }
}