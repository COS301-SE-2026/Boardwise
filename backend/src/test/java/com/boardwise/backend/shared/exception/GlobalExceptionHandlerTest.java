package com.boardwise.backend.shared.exception;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.boardwise.backend.vault.exception.ConcurrentModificationAnomalyException;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.R2PresignException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;

public class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup(){
        handler = new GlobalExceptionHandler();
    }

    private void assertStatusCodeAndMessage(ResponseEntity<Map<String, String>> response, HttpStatusCode statusCode, String message){
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getBody()).containsEntry("message", message);
    }

    private void assertStatusCodeAndMessage2(ResponseEntity<Map<String, Object>> response, HttpStatusCode statusCode, String message){
        assertThat(response.getStatusCode()).isEqualTo(statusCode);
        assertThat(response.getBody()).containsEntry("message", message);
    }

    @Test
    void handleNotFoundReturns404WithMessage(){
        // Arrange
        ObjectId id = new ObjectId();
        
        // Act
        var response = handler.handleNotFound(new RulebookNotFoundException(id));

        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.NOT_FOUND, "Rulebook not found: " + id);
    }

    @Test
    void handleForbiddenReturns403WithMessage(){
        // Act
        var response = handler.handleForbidden(new AccessDeniedException("no access"));
        
        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.FORBIDDEN, "no access");
    }
    
    @Test
    void handleLockConflictReturns409WithMessage(){
        // Act
        var response = handler.handleLockConflict(new LockConflictException("lock conflict"));

        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.CONFLICT, "lock conflict");
    }
    
    @Test
    void handleLockNotHeldReturns403WithMessage(){
        // Act
        var response = handler.handleLockNotHeld(new LockNotHeldException("lock not held"));
        
        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.FORBIDDEN, "lock not held");
    }
    
    @Test
    void handleVersionMismatchReturns409WithMessage(){
        // Arrange
        long expected = 5L;
        long actual = 4L;
        
        // Act
        var response = handler.handleVersionMismatch(new VersionMismatchException(expected, actual));

        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.CONFLICT, "Version mismatch - expected: " + expected + ", actual: " + actual);
    }
    
    @Test
    void handleR2FailureReturns502WithMessage(){
        // Act
        var response = handler.handleR2Failure(new R2PresignException("presign error"));
        
        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.BAD_GATEWAY, "presign error");
    }
    
    @Test
    void handleGenericReturns500WithMessage(){
        // Act
        var response = handler.handleGeneric(new Exception());
        
        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    @Test
    void handleBadRequestReturns400WithMessage(){
        // Act
        var response = handler.handleBadRequest(new IllegalArgumentException("bad input"));

        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.BAD_REQUEST, "bad input");
    }

    @Test
    void handleNoHandlerFoundIncludesRequestUrl(){
        // Arrange
        NoHandlerFoundException ex = new NoHandlerFoundException("GET", "/api/mock", new HttpHeaders());
        
        // Act
        var response = handler.handleNoHandlerFound(ex);
        
        // Assert
        assertThat(response.getBody()).containsEntry("message", "Endpoint not found: " + ex.getRequestURL());
    }

    @Test
    void handleValidationErrorsMapsSingleFieldError(){
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(new FieldError("obj", "title", "must not be blank")));
        
        // Act
        var response = handler.handleValidationErrors(ex);

        // Assert
        assertStatusCodeAndMessage2(response, HttpStatus.BAD_REQUEST, "Missing or invalid required fields");
        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) response.getBody().get("fields");
        assertThat(fields).containsEntry("title", "must not be blank");

    }
    
    @Test
    void handleValidationErrorsMapsMultipleFieldError(){
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
            new FieldError("obj", "title", "must not be blank"),
            new FieldError("obj", "duration", "must be a positive number")
        ));

        // Act
        var response = handler.handleValidationErrors(ex);

        // Assert
        assertStatusCodeAndMessage2(response, HttpStatus.BAD_REQUEST, "Missing or invalid required fields");
        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) response.getBody().get("fields");
        assertThat(fields).hasSize(2);
        assertThat(fields).containsEntry("title", "must not be blank");
        assertThat(fields).containsEntry("duration", "must be a positive number");
    }

    @Test
    void handleDuplicateKeyExceptionForUsernameReturnsUsernameMessage(){
        // Act
        var response = handler.handleDuplicateKeyException(new DuplicateKeyException("... username_1 ..."));
        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.CONFLICT, "Username is already taken");
    }
    
    @Test
    void handleDuplicateKeyExceptionForEmailReturnsEmailMessage(){
        // Act
        var response = handler.handleDuplicateKeyException(new DuplicateKeyException("... emailAddress_1 ..."));
        // Assert
        assertThat(response.getBody()).containsEntry("message", "Email address is already in use");
    }
    
    @Test
    void handleDuplicateKeyExceptionForOtherReturnsGenericMessage(){
        // Act
        var response = handler.handleDuplicateKeyException(new DuplicateKeyException("... some field ..."));
        // Assert
        assertThat(response.getBody()).containsEntry("message", "A duplicate entry already exists");
    }
    
    @Test
    void handleDuplicateKeyExceptionWithNullMessageReturnsGenericMessage(){
        // Act
        var response = handler.handleDuplicateKeyException(new DuplicateKeyException(null));
        // Assert
        assertThat(response.getBody()).containsEntry("message", "A duplicate entry already exists");
    }

    @Test
    void handleFailedUserDeletionReturns500WithMessage(){
        // Act
        var response = handler.handleFailedUserDeletion(new OptimisticLockingFailureException("failed"));

        // Assert
        assertStatusCodeAndMessage2(response, HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete account. Something went wrong on our side.");
    }

    @Test
    void handleConcurrentModificationAnomalyReturns500WithMessage(){
        // Act
        var response = handler.handleConcurrentModificationAnomaly(new ConcurrentModificationAnomalyException("error occurred"));

        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.INTERNAL_SERVER_ERROR, "error occurred");
    }
    
    @Test
    void handleIllegalStateReturns500WithMessage(){
        // Act
        var response = handler.handleIllegalState(new IllegalStateException("illegal state"));

        // Assert
        assertStatusCodeAndMessage(response, HttpStatus.INTERNAL_SERVER_ERROR, "illegal state");
    }
}