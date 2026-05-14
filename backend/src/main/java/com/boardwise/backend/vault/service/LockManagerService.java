package com.boardwise.backend.vault.service;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.boardwise.backend.vault.dto.response.LockResponseDto;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockManagerService {
    private static final int LOCK_EXPIRY_SECONDS = 30;

    private final WriteLockRepository writeLockRepository;
    private final RulebookRepository rulebookRepository;
    
    // AC-VLT-06: Acquire Write Lock
    public LockResponseDto acquireLock(ObjectId rulebookId, ObjectId userId){
        Rulebook rulebook = findRulebookOrThrow(rulebookId);

        writeLockRepository.findByRulebookId(rulebookId).ifPresent(existingLock -> {
            // Check if the lock has expired
            if(existingLock.getExpiresAt().isAfter(Instant.now())){
                throw new LockConflictException(rulebookId);
            }
            // expired lock - clear it before granting a new one
            writeLockRepository.delete(existingLock);
        });

        Instant now = Instant.now();
        WriteLock lock = WriteLock.builder()
            .rulebookId(rulebookId)
            .heldByUserId(userId)
            .acquiredAt(now)
            .expiresAt(now.plusSeconds(LOCK_EXPIRY_SECONDS))
            .build();

        writeLockRepository.save(lock);

        return LockResponseDto.builder()
            .lockGranted(true)
            .lockedBy(userId.toHexString())
            .expiresAt(lock.getExpiresAt())
            .currentVersion(rulebook.getVersion())
            .build();
    }

    // AC-VLT-08: Release Write Lock
    
    // AC-VLT-07: Commit Edit Delta

    // --- private helpers ---
    private Rulebook findRulebookOrThrow(ObjectId id){
        return rulebookRepository.findById(id)
            .orElseThrow(() -> new RulebookNotFoundException(id));
    }
}
