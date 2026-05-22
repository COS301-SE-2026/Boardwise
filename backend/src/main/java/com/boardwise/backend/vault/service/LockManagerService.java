package com.boardwise.backend.vault.service;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import com.boardwise.backend.vault.dto.request.CommitDeltaRequestDto;
import com.boardwise.backend.vault.dto.response.CommitDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.LockResponseDto;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.EditEventRepository;
import com.boardwise.backend.vault.repository.RulebookRepository;
import com.boardwise.backend.vault.repository.RulebookTextRepository;
import com.boardwise.backend.vault.repository.WriteLockRepository;
import com.boardwise.backend.vault.websocket.VaultEventPublisher;
import com.mongodb.DuplicateKeyException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LockManagerService {
    private static final int LOCK_EXPIRY_SECONDS = 30;

    private final WriteLockRepository writeLockRepository;
    private final RulebookRepository rulebookRepository;
    private final RulebookTextRepository rulebookTextRepository;
    private final EditEventRepository editEventRepository;

    private final VaultEventPublisher eventPublisher;
    
    // AC-VLT-06: Acquire Write Lock
    public LockResponseDto acquireLock(ObjectId rulebookId, ObjectId userId) {
        Rulebook rulebook = findRulebookOrThrow(rulebookId);

        // Clean up any lock that has already expired.
        // This is not atomic, but the subsequent insert is.
        writeLockRepository.findByRulebookId(rulebookId).ifPresent(existingLock -> {
            if (existingLock.getExpiresAt().isBefore(Instant.now())) {
                writeLockRepository.delete(existingLock);
            } else {
                throw new LockConflictException(rulebookId);
            }
        });

        Instant now = Instant.now();
        WriteLock lock = WriteLock.builder()
                .rulebookId(rulebookId)
                .heldByUserId(userId)
                .acquiredAt(now)
                .expiresAt(now.plusSeconds(LOCK_EXPIRY_SECONDS))
                .build();

        try {
            // Atomic insert – fails if a lock for this rulebook already exists
            writeLockRepository.insert(lock);
        } catch (DuplicateKeyException e) {
            throw new LockConflictException(rulebookId);
        }

        eventPublisher.publishLockAcquired(
                rulebookId,
                userId,
                lock.getExpiresAt(),
                rulebook.getVersion());

        return LockResponseDto.builder()
                .lockGranted(true)
                .lockedBy(userId.toHexString())
                .expiresAt(lock.getExpiresAt())
                .currentVersion(rulebook.getVersion())
                .build();
    }

    // AC-VLT-08: Release Write Lock
    public void releaseLock(ObjectId rulebookId, ObjectId userId) {
        findRulebookOrThrow(rulebookId);

        WriteLock lock = writeLockRepository.findByRulebookId(rulebookId)
            .orElseThrow(() -> new LockNotHeldException(userId));

        if(!lock.getHeldByUserId().equals(userId)){
            throw new LockNotHeldException(userId);
        }

        writeLockRepository.delete(lock);

        eventPublisher.publishLockReleased(rulebookId, userId, "voluntary");
    }

    // AC-VLT-07: Commit Edit Delta
    public CommitDeltaResponseDto commitDelta(ObjectId rulebookId, ObjectId userId, CommitDeltaRequestDto request){
        Rulebook rulebook = findRulebookOrThrow(rulebookId);

        // Verify that the caller holds the lock
        WriteLock lock = writeLockRepository.findByRulebookId(rulebookId)
            .orElseThrow(() -> new LockNotHeldException(userId));

        if(!lock.getHeldByUserId().equals(userId)){
            throw new LockNotHeldException(userId);
        }

        // Verify version matches
        RulebookText text = rulebookTextRepository.findByRulebookId(rulebookId)
            .orElseThrow(() -> new RulebookNotFoundException(rulebookId));

        if(text.getVersion() != request.getExpectedVersion()){
            throw new VersionMismatchException(request.getExpectedVersion(), text.getVersion());
        }

        // Commit delta
        Instant now = Instant.now();
        int newVersion = text.getVersion() + 1;

        text.setContent(request.getDelta());
        text.setVersion(newVersion);
        text.setUpdatedAt(now);
        rulebookTextRepository.save(text);

        // Update rulebook version
        rulebook.setVersion(newVersion);
        rulebook.setUpdatedAt(now);
        rulebookRepository.save(rulebook);

        // Append edit event
        EditEvent event = EditEvent.builder()
            .rulebookId(rulebookId)
            .editorId(userId)
            .delta(request.getDelta())
            .versionAfter(newVersion)
            .committedAt(now)
            .build();
        editEventRepository.save(event);

        eventPublisher.publishDelta(
                rulebookId,
                userId,
                request.getDelta(),
                newVersion,
                now);

        // refresh lock expiry on activity
        lock.setExpiresAt(now.plusSeconds(LOCK_EXPIRY_SECONDS));
        writeLockRepository.save(lock);

        return CommitDeltaResponseDto.builder()
            .committed(true)
            .newVersion(newVersion)
            .committedAt(now)
            .build();
    }
    // --- private helpers ---
    private Rulebook findRulebookOrThrow(ObjectId id){
        return rulebookRepository.findById(id)
            .orElseThrow(() -> new RulebookNotFoundException(id));
    }
}
