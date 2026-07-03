package com.boardwise.backend.vault.service;

import java.time.Instant;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.request.CommitEditDeltaRequestDto;
import com.boardwise.backend.vault.dto.response.AcquireWriteLockDto;
import com.boardwise.backend.vault.dto.response.CommitEditDeltaResponseDto;
import com.boardwise.backend.vault.exception.ConcurrentModificationAnomalyException;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WriteLockService {
    private final RulebookRepository rulebookRepository;
    private final UserRepository userRepository;
    private final RulebookTextRepository rulebookTextRepository;
    private final EditEventRepository editEventRepository;

    private static final int LOCK_TIMEOUT_MINUTES = 5;
    
    // AC-VLT-06: Aquire Write Lock
    public AcquireWriteLockDto acquireWriteLock(ObjectId rulebookId, ObjectId userId){
        // Check if user exists
        User user = findUserOrThrow(userId);

        // Attempt lock acquisition
        Instant newExpiry = Instant.now().plusSeconds(LOCK_TIMEOUT_MINUTES * 60);

        Rulebook lockedRulebook = rulebookRepository.atomicAcquireWriteLock(rulebookId, userId, newExpiry);
        
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
    @Transactional
    public CommitEditDeltaResponseDto commitEditDelta(ObjectId rulebookId, ObjectId userId, CommitEditDeltaRequestDto request){
        // Check if user exists
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();
        // Attempt commit edit delta
        // 1. Update RULEBOOK
        Rulebook rulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId, request.getExpectedVersion(), now.plusSeconds(LOCK_TIMEOUT_MINUTES * 60));
        if(rulebook == null){
            // Determine reason for failure and throw relevant exception
            // Check if rulebook exists
            Rulebook currentRulebook = findRulebookOrThrow(rulebookId);

            // Check if user owns the lock
            if(currentRulebook.getLockHeldBy() == null || !currentRulebook.getLockHeldBy().equals(userId)){
                throw new LockNotHeldException(userId);
            }

            // Check for a version mismatch
            if(currentRulebook.getVersion() != request.getExpectedVersion()){
                throw new VersionMismatchException(request.getExpectedVersion(), currentRulebook.getVersion());
            }

            // Fallback
            throw new ConcurrentModificationAnomalyException("Failed to commit delta due to concurrent state modification.");
        }

        // 2. Update RULEBOOK_TEXT
        rulebookTextRepository.atomicUpdateChunk(rulebookId, request.getChunkId(), request.getDeltaContent());
        
        // 3. Insert EDIT_EVENT
        EditEvent event = EditEvent.builder()
            .rulebookId(rulebookId)
            .editorId(new ObjectId(user.getId()))
            .delta(request.getDeltaContent())
            .versionAfter(rulebook.getVersion())
            .committedAt(now)
            .build();
        editEventRepository.save(event);

        return CommitEditDeltaResponseDto.builder()
            .commited(true)
            .newVersion(rulebook.getVersion())
            .committedAt(now)
            .build();
    }

    // AC-VLT-08: Release Write Lock
    public void releaseWriteLock(ObjectId rulebookId, ObjectId userId){
        // Validate user
        findUserOrThrow(userId);

        // Attempt lock release
        Rulebook rulebook = rulebookRepository.atomicReleaseWriteLock(rulebookId, userId);
        if(rulebook == null){
            Rulebook currentRulebook = findRulebookOrThrow(rulebookId);

            if(currentRulebook.getLockHeldBy() == null || !currentRulebook.getLockHeldBy().equals(userId)){
                throw new LockNotHeldException(userId);
            }

            throw new ConcurrentModificationAnomalyException("Failed to release the lock due to concurrent state modification.");
        }
    }

    // ----- Private Helpers -----
    private Rulebook findRulebookOrThrow(ObjectId id){
        return rulebookRepository.findById(id).orElseThrow(() -> new RulebookNotFoundException(id));
    }

    private User findUserOrThrow(ObjectId id){
        return userRepository.findById(id.toHexString())
            .orElseThrow(() -> new IllegalArgumentException("User does not exist."));
    }
}
