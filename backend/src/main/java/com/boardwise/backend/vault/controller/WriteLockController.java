package com.boardwise.backend.vault.controller;

import java.nio.file.attribute.UserPrincipal;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.user_service.models.UserDetailImpl;
import com.boardwise.backend.vault.dto.response.AcquireWriteLockDto;
import com.boardwise.backend.vault.service.WriteLockService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;


@CrossOrigin(origins="http://localhost:3000")
@RestController
@RequestMapping("/api/vault/rulebooks")
@RequiredArgsConstructor
public class WriteLockController {
    private final WriteLockService writeLockService;
    
    // AC-VLT-06: Aquire Write Lock
    @PostMapping("/{id}/lock")
    public ResponseEntity<AcquireWriteLockDto> getWriteLock(
        @PathVariable("id") ObjectId rulebookId,
        Authentication authentication) {
            UserDetailImpl userDetails = (UserDetailImpl) authentication.getPrincipal();
            ObjectId userId = new ObjectId(userDetails.getUserId());
        return ResponseEntity.ok(
            writeLockService.acquireWriteLock(rulebookId, userId)
        );
    }
    
    // AC-VLT-07: Commit Edit Delta
    // AC-VLT-08: Release Write Lock
}
