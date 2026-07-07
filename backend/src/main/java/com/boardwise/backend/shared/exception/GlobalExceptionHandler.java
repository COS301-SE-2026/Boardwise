package com.boardwise.backend.shared.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.NoActionsToRedoException;
import com.boardwise.backend.vault.exception.NoActionsToUndoException;
import com.boardwise.backend.vault.exception.R2PresignException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.BoardgameNotFoundException;
import com.boardwise.backend.vault.exception.ChunkNotFoundException;
import com.boardwise.backend.vault.exception.ConcurrentModificationAnomalyException;
import com.boardwise.backend.vault.exception.VersionMismatchException;
import com.mongodb.DuplicateKeyException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({RulebookNotFoundException.class, BoardgameNotFoundException.class, ChunkNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex){
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

    @ExceptionHandler({IllegalArgumentException.class, NoActionsToUndoException.class, NoActionsToRedoException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        Map<String, Object> res = new HashMap<>();
        res.put("error", "Missing or invalid required fields");
        res.put("fields", errors);

        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateKeyException(DuplicateKeyException ex) {
        Map<String, String> error = new HashMap<>();
        
        String message = ex.getMessage();
        
        if (message != null && message.contains("username")) {
            error.put("error", "Username is already taken");
        } else if (message != null && message.contains("emailAddress")) {
            error.put("error", "Email address is already in use");
        } else {
            error.put("error", "A duplicate entry already exists");
        }
        
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleFailedUserDeletion(OptimisticLockingFailureException ex){
        Map<String, Object> res = new HashMap<>();
        res.put("message", "Failed to delete account. Something went wrong on our side.");
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(R2PresignException.class)
    public ResponseEntity<Map<String, String>> handleR2Failure(R2PresignException ex){
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConcurrentModificationAnomalyException.class)
    public ResponseEntity<Map<String, String>> handleConcurrentModificationAnomaly(ConcurrentModificationAnomalyException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error","Internal Server Error","message",ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Internal Server Error","message",ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred"));
    }
}
