package com.boardwise.backend.vault.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.shared.security.JwtUtil;
import com.boardwise.backend.vault.dto.request.CommitDeltaRequestDto;
import com.boardwise.backend.vault.dto.response.CommitDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.LockResponseDto;
import com.boardwise.backend.vault.service.LockManagerService;

import lombok.RequiredArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/vault/rulebooks")
@RequiredArgsConstructor
public class LockController {
    private final LockManagerService lockManagerService;
    private final JwtUtil jwtUtil;

    // AC-VLT-06: Acquire Write Lock
    @PostMapping("/{id}/lock")
    public ResponseEntity<LockResponseDto> acquireLock(
        @PathVariable String id,
        Authentication authentication) {
            ObjectId userId = extractUserId(authentication);
        
        return ResponseEntity.ok(
            lockManagerService.acquireLock(toObjectId(id), userId)
        );
    }

    // AC-VLT-08: Release Write Lock
    @DeleteMapping("/{id}/lock")
    public ResponseEntity<Void> releaseLock(
        @PathVariable String id,
        Authentication authentication){
            ObjectId userId = extractUserId(authentication);
            lockManagerService.releaseLock(toObjectId(id), userId);
            return ResponseEntity.ok().build();
    }

    // AC-VLT-07: Commit Edit Delta
    @PatchMapping("/{id}/text")
    public ResponseEntity<CommitDeltaResponseDto> commitDelta(
        @PathVariable String id,
        @RequestBody CommitDeltaRequestDto request,
        Authentication authentication){
            ObjectId userId = extractUserId(authentication);
            return ResponseEntity.ok(
                lockManagerService.commitDelta(toObjectId(id), userId, request)
            );
    }

    // ----- private helpers -----
    private ObjectId extractUserId(Authentication authentication){
        String token = (String) authentication.getCredentials();
        return jwtUtil.extractUserId(token);
    }

    private ObjectId toObjectId(String id){
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid rulebook ID format: " + id);
        }
    }
}
