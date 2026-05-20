package com.boardwise.backend.vault.exception;

public class VersionMismatchException extends RuntimeException {
    public VersionMismatchException(int expected, int actual) {
        super("Version mismatch - expected: " + expected + ", actual: " + actual);
    }
}