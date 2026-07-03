package com.boardwise.backend.vault.service;

import java.time.Instant;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.response.AcquireWriteLockDto;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.RulebookRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WriteLockService {
    private final RulebookRepository rulebookRepository;
    private final UserRepository userRepository;

    private static final int LOCK_TIMEOUT_MINUTES = 5;
    
    // AC-VLT-06: Aquire Write Lock
    public AcquireWriteLockDto acquireWriteLock(ObjectId rulebookId, ObjectId userId){
        // Check if user exists
        User user = findUserOrThrow(userId);

        // Attempt lock acquisition
        Instant newExpiry = Instant.now().plusSeconds(LOCK_TIMEOUT_MINUTES * 60);

        Rulebook lockedRulebook = rulebookRepository.atomicAcquireWriteLock(rulebookId.toHexString(), userId.toHexString(), newExpiry);
        
        if(lockedRulebook == null){
            // Check if rulebook exists
            boolean exists = rulebookRepository.existsById(rulebookId);
            if(!exists){
                throw new RulebookNotFoundException(rulebookId);
            }else{
                throw new LockConflictException(rulebookId);
            }
        }
        return AcquireWriteLockDto.builder()
            .lockGranted(true)
            .lockedBy(user.getUsername())
            .expiresAt(newExpiry)
            .currentVersion(lockedRulebook.getVersion())
            .build();
    }

    // AC-VLT-07: Commit Edit Delta
    
    // AC-VLT-08: Release Write Lock

    // ----- Private Helpers -----
    private User findUserOrThrow(ObjectId id){
        return userRepository.findById(id.toHexString())
            .orElseThrow(() -> new IllegalArgumentException("User does not exist."));
    }
}
