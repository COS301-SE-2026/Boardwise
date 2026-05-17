package com.boardwise.backend.user_service.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

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
}
