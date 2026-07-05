package com.boardwise.backend.vault.service;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.request.CommitEditDeltaRequestDto;
import com.boardwise.backend.vault.dto.request.InsertNewChunkRequestDto;
import com.boardwise.backend.vault.dto.response.AcquireWriteLockDto;
import com.boardwise.backend.vault.dto.response.CommitEditDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.InsertNewChunkResponseDto;
import com.boardwise.backend.vault.dto.websocket.ChunkInsertedEventDto;
import com.boardwise.backend.vault.dto.websocket.DeltaCommitedEventDto;
import com.boardwise.backend.vault.dto.websocket.LockAcquiredEventDto;
import com.boardwise.backend.vault.dto.websocket.LockReleasedEventDto;
import com.boardwise.backend.vault.enums.EditType;
import com.boardwise.backend.vault.exception.ConcurrentModificationAnomalyException;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.exception.VersionMismatchException;
import com.boardwise.backend.vault.model.EditEvent;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.model.RulebookText;
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

    private final ApplicationEventPublisher eventPublisher;

    private static final int LOCK_TIMEOUT_MINUTES = 5;
    
    // AC-VLT-06: Aquire Write Lock
    @Transactional
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

        eventPublisher.publishEvent(new LockAcquiredEventDto(
            rulebookId.toHexString(),
            userId.toHexString(),
            user.getUsername(),
            newExpiry,
            lockedRulebook.getVersion()
        ));

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

        ObjectId targetChunkId = new ObjectId(request.getChunkId());
        
        // 2. Fetch Previous Text State
        RulebookText chunkBeforeUpdate = rulebookTextRepository.findBySpecificChunk(rulebookId, targetChunkId)
            .orElseThrow(() -> new IllegalArgumentException("Target rulebook or chunk does not exist."));
        String previousText = chunkBeforeUpdate.getChunks().get(0).getContent();

        // 3. Update RULEBOOK_TEXT
        rulebookTextRepository.atomicUpdateChunk(rulebookId, new ObjectId(request.getChunkId()), request.getDeltaContent());
        
        // 4. Insert EDIT_EVENT
        EditEvent event = EditEvent.builder()
            .rulebookId(rulebookId)
            .editorId(new ObjectId(user.getId()))
            .chunkId(new ObjectId(request.getChunkId()))
            .editType(EditType.UPDATE)
            .previousContent(previousText)
            .newContent(request.getDeltaContent())
            .versionAfter(rulebook.getVersion())
            .committedAt(now)
            .build();
        editEventRepository.save(event);

        eventPublisher.publishEvent(DeltaCommitedEventDto.builder()
            .rulebookId(rulebookId.toHexString())
            .chunkId(request.getChunkId())
            .deltaContent(request.getDeltaContent())
            .version(rulebook.getVersion())
            .build()
        );

        return CommitEditDeltaResponseDto.builder()
            .committed(true)
            .newVersion(rulebook.getVersion())
            .committedAt(now)
            .build();
    }

    // AC-VLT-08: Release Write Lock
    @Transactional
    public void releaseWriteLock(ObjectId rulebookId, ObjectId userId){
        // Validate user
        User user = findUserOrThrow(userId);

        // Attempt lock release
        Rulebook rulebook = rulebookRepository.atomicReleaseWriteLock(rulebookId, userId);
        if(rulebook == null){
            Rulebook currentRulebook = findRulebookOrThrow(rulebookId);

            if(currentRulebook.getLockHeldBy() == null || !currentRulebook.getLockHeldBy().equals(userId)){
                throw new LockNotHeldException(userId);
            }

            throw new ConcurrentModificationAnomalyException("Failed to release the lock due to concurrent state modification.");
        }
        eventPublisher.publishEvent(new LockReleasedEventDto(
            rulebookId.toHexString(),
            userId.toHexString(),
            user.getUsername(),
            "voluntary",
            Instant.now()
        ));
    }

    @Transactional
    public void releaseAllWriteLocksForUser(ObjectId userId){
        // Validate user
        User user = findUserOrThrow(userId);

        // Find all rulebooks locked by user
        List<Rulebook> lockedRulebooks = rulebookRepository.findByLockHeldBy(userId);

        if(!lockedRulebooks.isEmpty()){
            // Attempt multi-lock release
            rulebookRepository.atomicReleaseAllWriteLocks(userId);

            // Broadcast release events per rulebook
            for(Rulebook rulebook: lockedRulebooks){
                eventPublisher.publishEvent(new LockReleasedEventDto(
                    rulebook.getId().toHexString(),
                    userId.toHexString(),
                    user.getUsername(),
                    "disconnected",
                    Instant.now()
                ));
            }
        }
    }

    @Transactional
    public InsertNewChunkResponseDto insertNewChunk(ObjectId rulebookId, ObjectId userId, InsertNewChunkRequestDto request){
        // Validate user
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();

        // Attempt insert chunk
        // 1. Update RULEBOOK
        Rulebook rulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId,
                request.getExpectedVersion(), now.plusSeconds(LOCK_TIMEOUT_MINUTES * 60));
        if(rulebook == null){
            // Determine failure reason and throw appropriate error.
            // Check if rulebook exists
            Rulebook currentRulebook = findRulebookOrThrow(rulebookId);

            // Check if user owns the lock
            if (currentRulebook.getLockHeldBy() == null || !currentRulebook.getLockHeldBy().equals(userId)) {
                throw new LockNotHeldException(userId);
            }

            // Check for a version mismatch
            if (currentRulebook.getVersion() != request.getExpectedVersion()) {
                throw new VersionMismatchException(request.getExpectedVersion(), currentRulebook.getVersion());
            }

            // Fallback
            throw new ConcurrentModificationAnomalyException(
                    "Failed to insert new chunk due to concurrent state modification.");
        }

        // 2. Update RULEBOOK_TEXT
        ObjectId chunkId = new ObjectId();
        boolean inserted = rulebookTextRepository.atomicInsertChunk(rulebookId, chunkId, request.getContent(),
                request.getInsertIndex(), request.getLastIndex());

        // 3. Insert EDIT_EVENT
        EditEvent event = EditEvent.builder()
                .rulebookId(rulebookId)
                .editorId(new ObjectId(user.getId()))
                .chunkId(chunkId)
                .editType(EditType.INSERT)
                .previousContent(null)
                .newContent(request.getContent())
                .versionAfter(rulebook.getVersion())
                .committedAt(now)
                .build();
        editEventRepository.save(event);

        int calculatedIndex = (request.getInsertIndex() < 0 || request.getInsertIndex() > request.getLastIndex())
                ? request.getLastIndex() + 1
                : request.getInsertIndex();

        eventPublisher.publishEvent(ChunkInsertedEventDto.builder()
            .rulebookId(rulebookId.toHexString())
            .chunkId(chunkId.toHexString())
            .content(request.getContent())
            .index(calculatedIndex)
            .version(rulebook.getVersion())
            .build());
        
        return InsertNewChunkResponseDto.builder()
            .inserted(inserted)
            .newVersion(rulebook.getVersion())
            .chunkId(chunkId.toHexString())
            .actualIndex(calculatedIndex)
            .insertedAt(now)
            .build();
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
