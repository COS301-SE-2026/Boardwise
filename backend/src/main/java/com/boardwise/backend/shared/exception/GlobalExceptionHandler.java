package com.boardwise.backend.shared.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RulebookNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RulebookNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(LockConflictException.class)
    public ResponseEntity<Map<String, String>> handleLockConflict(LockConflictException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Endpoint not found: " + ex.getRequestURL()));
    }

    @ExceptionHandler(LockNotHeldException.class)
    public ResponseEntity<Map<String, String>> handleLockNotHeld(
        LockNotHeldException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(VersionMismatchException.class)
    public ResponseEntity<Map<String, String>> handleVersionMismatch(
        VersionMismatchException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
    }
}
