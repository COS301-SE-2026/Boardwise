package com.boardwise.backend.shared.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.bson.BsonDocument;
import com.mongodb.ServerAddress;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;
import com.mongodb.DuplicateKeyException;

// We remove all the complex Spring Boot annotations and test this handler purely as a unit!
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Manually build MockMvc with our test controller and the exception handler
        // This guarantees the routes exist and bypasses all Spring Security filters
        // entirely!
        this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // --- 1. VALIDATION TESTS ----------------------------------------------------

    @Test
    void shouldHandleValidationErrors() throws Exception {
        String invalidJson = """
                {
                    "username": "ab",
                    "password": "pwd"
                }
                """;

        mockMvc.perform(post("/api/auth/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing or invalid required fields"))
                .andExpect(jsonPath("$.fields.username").exists())
                .andExpect(jsonPath("$.fields.password").exists());
    }

    @Test
    void shouldHandleMissingFields() throws Exception {
        String emptyJson = "{}";

        mockMvc.perform(post("/api/auth/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(emptyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing or invalid required fields"));
    }

    // --- 2. VAULT & BUSINESS EXCEPTIONS COVERAGE ---------------------------------

    @Test
    void shouldHandleRulebookNotFoundException() throws Exception {
        mockMvc.perform(get("/api/auth/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldHandleLockConflictException() throws Exception {
        mockMvc.perform(get("/api/auth/test/lock-conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldHandleAccessDeniedException() throws Exception {
        mockMvc.perform(get("/api/auth/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/api/auth/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldHandleLockNotHeldException() throws Exception {
        mockMvc.perform(get("/api/auth/test/lock-not-held"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldHandleVersionMismatchException() throws Exception {
        mockMvc.perform(get("/api/auth/test/version-mismatch"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- 3. MONGODB DUPLICATE KEY PARSING COVERAGE ------------------------------

    @Test
    void shouldHandleDuplicateKeyException_Username() throws Exception {
        mockMvc.perform(get("/api/auth/test/duplicate-username"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username is already taken"));
    }

    @Test
    void shouldHandleDuplicateKeyException_Email() throws Exception {
        mockMvc.perform(get("/api/auth/test/duplicate-email"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email address is already in use"));
    }

    @Test
    void shouldHandleDuplicateKeyException_Generic() throws Exception {
        mockMvc.perform(get("/api/auth/test/duplicate-generic"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("A duplicate entry already exists"));
    }

    // --- 4. CATCH-ALL GENERIC EXCEPTIONS ----------------------------------------

    @Test
    void shouldHandleGenericException() throws Exception {
        mockMvc.perform(get("/api/auth/test/generic-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    // --- INNER TEST CONTROLLER FOR ISOLATED TESTING -----------------------------

    @RestController
    public static class TestController {

        @PostMapping("/api/auth/test/validation")
        public void testValidation(@Valid @RequestBody DummyDTO dto) {
        }

        @GetMapping("/api/auth/test/not-found")
        public void throwNotFound() {
            throw new RulebookNotFoundException("Rulebook not found");
        }

        @GetMapping("/api/auth/test/lock-conflict")
        public void throwLockConflict() {
            throw new LockConflictException("Lock conflict detected");
        }

        @GetMapping("/api/auth/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Access Denied");
        }

        @GetMapping("/api/auth/test/illegal-argument")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("Invalid argument passed");
        }

        @GetMapping("/api/auth/test/lock-not-held")
        public void throwLockNotHeld() {
            throw new LockNotHeldException("Lock is not held");
        }

        @GetMapping("/api/auth/test/version-mismatch")
        public void throwVersionMismatch() {
            throw new VersionMismatchException(1, 2);
        }

        @GetMapping("/api/auth/test/duplicate-username")
        public void throwDupUsername() {
            throw new DuplicateKeyException(new BsonDocument(), new ServerAddress(), null) {
                @Override
                public String getMessage() {
                    return "username";
                }
            };
        }

        @GetMapping("/api/auth/test/duplicate-email")
        public void throwDupEmail() {
            throw new DuplicateKeyException(new BsonDocument(), new ServerAddress(), null) {
                @Override
                public String getMessage() {
                    return "emailAddress";
                }
            };
        }

        @GetMapping("/api/auth/test/duplicate-generic")
        public void throwDupGeneric() {
            throw new DuplicateKeyException(new BsonDocument(), new ServerAddress(), null) {
                @Override
                public String getMessage() {
                    return "generic crash";
                }
            };
        }

        @GetMapping("/api/auth/test/generic-error")
        public void throwGeneric() throws Exception {
            throw new Exception("NullPointerException or similar runtime crash");
        }
    }

    // Temporary DTO to isolate field validation annotation tracking
    static class DummyDTO {
        @NotBlank
        @Size(min = 3)
        private String username;
        @NotBlank
        @Size(min = 6)
        private String password;

        public DummyDTO(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}