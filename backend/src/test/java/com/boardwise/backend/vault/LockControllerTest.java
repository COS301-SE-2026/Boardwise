package com.boardwise.backend.vault;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardwise.backend.shared.exception.GlobalExceptionHandler;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.vault.controller.LockController;
import com.boardwise.backend.vault.dto.response.CommitDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.LockResponseDto;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;
import com.boardwise.backend.vault.service.LockManagerService;

@WebMvcTest(LockController.class)
@Import(GlobalExceptionHandler.class)
public class LockControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private LockManagerService lockManagerService;

        // LockController calls jwtService.extractUserId(token) inside every handler.
        // Must be mocked so the controller can extract a userId from the @WithMockUser
        // credentials.
        @MockitoBean
        private JWTService jwtService;

        private ObjectId rulebookId;
        private ObjectId userId;
        private LockResponseDto mockLockResponse;
        private CommitDeltaResponseDto mockCommitDeltaResponse;

        @BeforeEach
        void setUp() {
                rulebookId = new ObjectId();
                userId = new ObjectId();

                mockLockResponse = LockResponseDto.builder()
                                .lockGranted(true)
                                .lockedBy(userId.toHexString())
                                .expiresAt(Instant.now().plusSeconds(30))
                                .currentVersion(3)
                                .build();

                mockCommitDeltaResponse = CommitDeltaResponseDto.builder()
                                .committed(true)
                                .newVersion(4)
                                .committedAt(Instant.now())
                                .build();

                // Stub extractUserId for every token value; @WithMockUser sets credentials to
                // ""
                when(jwtService.extractUserId(any())).thenReturn(userId);
        }

        // -------------------------------------------------------------------------
        // POST /api/vault/rulebooks/{id}/lock
        // -------------------------------------------------------------------------

        @Test
        @WithMockUser
        void acquireLock_returns200WhenGranted() throws Exception {
                when(lockManagerService.acquireLock(any(ObjectId.class), any(ObjectId.class)))
                                .thenReturn(mockLockResponse);

                mockMvc.perform(post("/api/vault/rulebooks/{id}/lock", rulebookId.toHexString())
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.lockGranted").value(true))
                                .andExpect(jsonPath("$.lockedBy").value(userId.toHexString()))
                                .andExpect(jsonPath("$.currentVersion").value(3));
        }

        @Test
        @WithMockUser
        void acquireLock_returns409WhenLockAlreadyHeld() throws Exception {
                when(lockManagerService.acquireLock(any(ObjectId.class), any(ObjectId.class)))
                                .thenThrow(new LockConflictException(rulebookId));

                mockMvc.perform(post("/api/vault/rulebooks/{id}/lock", rulebookId.toHexString())
                                .with(csrf()))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void acquireLock_returns404WhenRulebookNotFound() throws Exception {
                when(lockManagerService.acquireLock(any(ObjectId.class), any(ObjectId.class)))
                                .thenThrow(new RulebookNotFoundException(rulebookId));

                mockMvc.perform(post("/api/vault/rulebooks/{id}/lock", rulebookId.toHexString())
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void acquireLock_returns400ForMalformedId() throws Exception {
                // The controller's toObjectId() helper throws IllegalArgumentException for a
                // non-ObjectId string, which GlobalExceptionHandler maps to 400.
                mockMvc.perform(post("/api/vault/rulebooks/{id}/lock", "not-a-valid-id")
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }

        // -------------------------------------------------------------------------
        // DELETE /api/vault/rulebooks/{id}/lock
        // -------------------------------------------------------------------------

        @Test
        @WithMockUser
        void releaseLock_returns200WhenReleased() throws Exception {
                doNothing().when(lockManagerService)
                                .releaseLock(any(ObjectId.class), any(ObjectId.class));

                mockMvc.perform(delete("/api/vault/rulebooks/{id}/lock", rulebookId.toHexString())
                                .with(csrf()))
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void releaseLock_returns403WhenCallerIsNotLockHolder() throws Exception {
                doThrow(new LockNotHeldException(userId))
                                .when(lockManagerService)
                                .releaseLock(any(ObjectId.class), any(ObjectId.class));

                mockMvc.perform(delete("/api/vault/rulebooks/{id}/lock", rulebookId.toHexString())
                                .with(csrf()))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void releaseLock_returns404WhenRulebookNotFound() throws Exception {
                doThrow(new RulebookNotFoundException(rulebookId))
                                .when(lockManagerService)
                                .releaseLock(any(ObjectId.class), any(ObjectId.class));

                mockMvc.perform(delete("/api/vault/rulebooks/{id}/lock", rulebookId.toHexString())
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void releaseLock_returns400ForMalformedId() throws Exception {
                mockMvc.perform(delete("/api/vault/rulebooks/{id}/lock", "not-a-valid-id")
                                .with(csrf()))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }

        // -------------------------------------------------------------------------
        // PATCH /api/vault/rulebooks/{id}/text
        // -------------------------------------------------------------------------

        @Test
        @WithMockUser
        void commitDelta_returns200WhenCommitted() throws Exception {
                when(lockManagerService.commitDelta(
                                any(ObjectId.class), any(ObjectId.class), any()))
                                .thenReturn(mockCommitDeltaResponse);

                mockMvc.perform(patch("/api/vault/rulebooks/{id}/text", rulebookId.toHexString())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "expectedVersion": 3,
                                                    "delta": "Updated content."
                                                }
                                                """))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.committed").value(true))
                                .andExpect(jsonPath("$.newVersion").value(4));
        }

        @Test
        @WithMockUser
        void commitDelta_returns403WhenCallerIsNotLockHolder() throws Exception {
                when(lockManagerService.commitDelta(
                                any(ObjectId.class), any(ObjectId.class), any()))
                                .thenThrow(new LockNotHeldException(userId));

                mockMvc.perform(patch("/api/vault/rulebooks/{id}/text", rulebookId.toHexString())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "expectedVersion": 3,
                                                    "delta": "Updated content."
                                                }
                                                """))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void commitDelta_returns409OnVersionMismatch() throws Exception {
                when(lockManagerService.commitDelta(
                                any(ObjectId.class), any(ObjectId.class), any()))
                                .thenThrow(new VersionMismatchException(1, 3));

                mockMvc.perform(patch("/api/vault/rulebooks/{id}/text", rulebookId.toHexString())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "expectedVersion": 1,
                                                    "delta": "Updated content."
                                                }
                                                """))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void commitDelta_returns404WhenRulebookNotFound() throws Exception {
                when(lockManagerService.commitDelta(
                                any(ObjectId.class), any(ObjectId.class), any()))
                                .thenThrow(new RulebookNotFoundException(rulebookId));

                mockMvc.perform(patch("/api/vault/rulebooks/{id}/text", rulebookId.toHexString())
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "expectedVersion": 3,
                                                    "delta": "Updated content."
                                                }
                                                """))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @WithMockUser
        void commitDelta_returns400ForMalformedId() throws Exception {
                mockMvc.perform(patch("/api/vault/rulebooks/{id}/text", "not-a-valid-id")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "expectedVersion": 3,
                                                    "delta": "Updated content."
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }
}