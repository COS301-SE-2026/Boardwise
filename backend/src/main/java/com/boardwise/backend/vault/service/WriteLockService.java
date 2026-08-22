package com.boardwise.backend.vault.service;

import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.boardwise.backend.vault.dto.request.CommitEditDeltaOrDoActionRequestDto;
import com.boardwise.backend.vault.dto.request.DeleteChunkRequestDto;
import com.boardwise.backend.vault.dto.request.InsertNewChunkRequestDto;
import com.boardwise.backend.vault.dto.request.VaultBaseRequestDto;
import com.boardwise.backend.vault.dto.response.AcquireWriteLockDto;
import com.boardwise.backend.vault.dto.response.CommitEditDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.DeleteChunkResponseDto;
import com.boardwise.backend.vault.dto.response.InsertNewChunkResponseDto;
import com.boardwise.backend.vault.dto.response.UndoOrRedoActionResponseDto;
import com.boardwise.backend.vault.dto.websocket.ChunkDeletedEventDto;
import com.boardwise.backend.vault.dto.websocket.ChunkInsertedEventDto;
import com.boardwise.backend.vault.dto.websocket.DeltaCommitedEventDto;
import com.boardwise.backend.vault.dto.websocket.LockAcquiredEventDto;
import com.boardwise.backend.vault.dto.websocket.LockReleasedEventDto;
import com.boardwise.backend.vault.enums.EditType;
import com.boardwise.backend.vault.exception.ChunkNotFoundException;
import com.boardwise.backend.vault.exception.ConcurrentModificationAnomalyException;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
import com.boardwise.backend.vault.exception.NoActionsToRedoException;
import com.boardwise.backend.vault.exception.NoActionsToUndoException;
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
        User user = findUserOrThrow(userId);

        Instant newExpiry = Instant.now().plusSeconds(LOCK_TIMEOUT_MINUTES * 60);

        Rulebook lockedRulebook = rulebookRepository.atomicAcquireWriteLock(rulebookId, userId, newExpiry);
        
        if(lockedRulebook == null){
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
    public CommitEditDeltaResponseDto commitEditDelta(ObjectId rulebookId, ObjectId userId, CommitEditDeltaOrDoActionRequestDto request){
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();
        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "edit");

        long nextVersion = rulebook.getVersion();

        ObjectId targetChunkId = new ObjectId(request.getChunkId());
        
        RulebookText chunkBeforeUpdate = rulebookTextRepository.findById(targetChunkId)
            .orElseThrow(() -> new ChunkNotFoundException(rulebookId, targetChunkId));
        String previousText = chunkBeforeUpdate.getContent();

        rulebookTextRepository.atomicUpdateChunk(rulebookId, targetChunkId, request.getContent());

        rulebookRepository.atomicCommitForwardEdit(rulebookId, nextVersion);
        
        EditEvent event = EditEvent.builder()
            .rulebookId(rulebookId)
            .editorId(new ObjectId(user.getId()))
            .chunkId(targetChunkId)
            .chunkBefore(null)
            .editType(EditType.UPDATE)
            .previousContent(previousText)
            .newContent(request.getContent())
            .versionPostEdit(rulebook.getVersion())
            .committedAt(now)
            .build();
        editEventRepository.save(event);

        eventPublisher.publishEvent(DeltaCommitedEventDto.builder()
            .rulebookId(rulebookId.toHexString())
            .chunkId(request.getChunkId())
            .deltaContent(request.getContent())
            .version(rulebook.getVersion())
            .build()
        );

        return CommitEditDeltaResponseDto.builder()
            .committed(true)
            .newVersion(rulebook.getVersion())
            .committedAt(now)
            .lockExpiresAt(rulebook.getLockExpiresAt())
            .build();
    }

    // AC-VLT-08: Release Write Lock
    @Transactional
    public void releaseWriteLock(ObjectId rulebookId, ObjectId userId){
        User user = findUserOrThrow(userId);

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
        User user = findUserOrThrow(userId);

        List<Rulebook> lockedRulebooks = rulebookRepository.findByLockHeldBy(userId);

        if(!lockedRulebooks.isEmpty()){
            rulebookRepository.atomicReleaseAllWriteLocks(userId);

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
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();

        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "insert");

        long nextVersion = rulebook.getVersion();

        RulebookText insertedDocument = rulebookTextRepository.atomicInsertChunk(rulebookId, request.getContent(), request.getInsertIndex());

        if(insertedDocument == null){
            throw new ConcurrentModificationAnomalyException("Failed to insert chunk.");
        }

        int actualAssignedIndex = insertedDocument.getIndex();

        rulebookRepository.atomicCommitForwardEdit(rulebookId, nextVersion);

        ObjectId chunkId = insertedDocument.getChunkId();

        EditEvent event = EditEvent.builder()
                .rulebookId(rulebookId)
                .editorId(new ObjectId(user.getId()))
                .chunkId(chunkId)
                .chunkBefore(null)
                .editType(EditType.INSERT)
                .previousContent(null)
                .newContent(request.getContent())
                .index(actualAssignedIndex)
                .versionPostEdit(rulebook.getVersion())
                .committedAt(now)
                .build();
        editEventRepository.save(event);

        eventPublisher.publishEvent(ChunkInsertedEventDto.builder()
            .eventType("CHUNK_INSERTED")
            .rulebookId(rulebookId.toHexString())
            .editorId(userId.toHexString())
            .version(rulebook.getVersion())
            .timestamp(now)
            .chunkId(chunkId.toHexString())
            .content(request.getContent())
            .index(actualAssignedIndex)
            .build());
        
        return InsertNewChunkResponseDto.builder()
            .inserted(true)
            .newVersion(rulebook.getVersion())
            .chunkId(chunkId.toHexString())
            .actualIndex(actualAssignedIndex)
            .insertedAt(now)
            .build();
    }

    @Transactional
    public DeleteChunkResponseDto removeChunk(ObjectId rulebookId, ObjectId userId, DeleteChunkRequestDto request){
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();

        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "delete");

        long nextVersion = rulebook.getVersion();
        
        ObjectId chunkToDeleteId = new ObjectId(request.getChunkId());
        RulebookText chunkBeforeDelete = rulebookTextRepository.findById(chunkToDeleteId)
            .orElseThrow(() -> new ChunkNotFoundException(rulebookId, chunkToDeleteId));
        String actualPreviousText = chunkBeforeDelete.getContent();
        int actualPreviousIndex = chunkBeforeDelete.getIndex();
        boolean deleted = rulebookTextRepository.atomicDeleteChunk(rulebookId, chunkToDeleteId);

        rulebookRepository.atomicCommitForwardEdit(rulebookId, nextVersion);
        
        EditEvent event = EditEvent.builder()
                .rulebookId(rulebookId)
                .editorId(new ObjectId(user.getId()))
                .chunkId(chunkToDeleteId)
                .chunkBefore(request.getChunkBeforeId() != null ? new ObjectId(request.getChunkBeforeId()) : null)
                .editType(EditType.DELETE)
                .previousContent(actualPreviousText)
                .index(actualPreviousIndex)
                .newContent(null)
                .versionPostEdit(rulebook.getVersion())
                .committedAt(now)
                .build();
        editEventRepository.save(event);

        eventPublisher.publishEvent(ChunkDeletedEventDto.builder()
                .eventType("CHUNK_DELETED")
                .rulebookId(rulebookId.toHexString())
                .editorId(userId.toHexString())
                .version(rulebook.getVersion())
                .timestamp(now)
                .chunkId(request.getChunkId())
                .build());

        return DeleteChunkResponseDto.builder()
                .deleted(deleted)
                .newVersion(rulebook.getVersion())
                .chunkId(request.getChunkId())
                .deletedAt(now)
                .build();
    }

    @Transactional
    public UndoOrRedoActionResponseDto undoAction(ObjectId rulebookId, ObjectId userId, CommitEditDeltaOrDoActionRequestDto request){
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();

        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "undo");

        if(rulebook.getUndoStack().isEmpty()){
            throw new NoActionsToUndoException(rulebookId);
        }

        Long targetVersion = rulebookRepository.atomicPopUndoAndPushRedo(rulebookId, userId);
        long newVersion = rulebook.getVersion();

        EditEvent targetEvent = editEventRepository.findByRulebookIdAndVersionPostEdit(rulebookId, targetVersion).orElseThrow(
           () -> new IllegalStateException("Database corruption. undoStack pointed to a version that does not exist in the EDIT_EVENT ledger")
        );

        String broadcastEventType = "";
        EditType inverseEditType;
        String eventNewContent = null;
        String eventPreviousContent = null;

        Integer eventIndex = null;
        int actualRestoredIndex = -1;

        switch (targetEvent.getEditType()) {
            case EditType.INSERT:
                rulebookTextRepository.atomicDeleteChunk(rulebookId, targetEvent.getChunkId());
                broadcastEventType = "CHUNK_DELETED";
                inverseEditType = EditType.DELETE;
                eventPreviousContent = targetEvent.getNewContent();
                
                eventIndex = targetEvent.getIndex();
                break;
            case EditType.DELETE:
                int targetIndex = targetEvent.getIndex();

                RulebookText updatedDocument = rulebookTextRepository.atomicInsertChunk(rulebookId, targetEvent.getPreviousContent(), targetIndex);

                if (updatedDocument == null) {
                    throw new ConcurrentModificationAnomalyException("Failed to insert chunk.");
                }

                actualRestoredIndex = updatedDocument.getIndex();

                broadcastEventType = "CHUNK_INSERTED";
                inverseEditType = EditType.INSERT;
                eventNewContent = targetEvent.getPreviousContent();

                eventIndex = actualRestoredIndex;
                break;
            case EditType.UPDATE:
                rulebookTextRepository.atomicUpdateChunk(rulebookId, targetEvent.getChunkId(), targetEvent.getPreviousContent());
                broadcastEventType = "DELTA_COMMITTED";
                inverseEditType = EditType.UPDATE;
                eventNewContent = targetEvent.getPreviousContent();
                eventPreviousContent = targetEvent.getNewContent();
                
                eventIndex = targetEvent.getIndex();
                break;
            default:
                throw new IllegalArgumentException(targetEvent.getEditType() +" is not a valid edit type.");
        }

        EditEvent event = EditEvent.builder()
                .rulebookId(rulebookId)
                .editorId(new ObjectId(user.getId()))
                .chunkId(targetEvent.getChunkId())
                .chunkBefore(targetEvent.getChunkBefore())
                .editType(inverseEditType)
                .previousContent(eventPreviousContent)
                .newContent(eventNewContent)
                .index(eventIndex)
                .versionPostEdit(newVersion)
                .compensatesVersion(targetVersion)
                .committedAt(now)
                .build();
        editEventRepository.save(event);

        switch (broadcastEventType) {
            case "CHUNK_INSERTED":
                eventPublisher.publishEvent(ChunkInsertedEventDto.builder()
                        .eventType("CHUNK_INSERTED")
                        .rulebookId(rulebookId.toHexString())
                        .editorId(userId.toHexString())
                        .version(newVersion)
                        .timestamp(now)
                        .chunkId(targetEvent.getChunkId().toHexString())
                        .content(eventNewContent)
                        .index(actualRestoredIndex)
                        .build());
                break;
            case "CHUNK_DELETED":
                eventPublisher.publishEvent(ChunkDeletedEventDto.builder()
                    .eventType("CHUNK_DELETED")
                    .rulebookId(rulebookId.toHexString())
                    .editorId(userId.toHexString())
                    .version(newVersion)
                    .timestamp(now)
                    .chunkId(targetEvent.getChunkId().toHexString())
                    .build());
                break;
            case "DELTA_COMMITTED":
                eventPublisher.publishEvent(DeltaCommitedEventDto.builder()
                    .eventType("DELTA_COMMITTED")
                    .rulebookId(rulebookId.toHexString())
                    .editorId(userId.toHexString())
                    .timestamp(now)
                    .chunkId(targetEvent.getChunkId().toHexString())
                    .deltaContent(eventNewContent)
                    .version(newVersion)
                    .build());
                break;
        }

        return UndoOrRedoActionResponseDto.builder()
            .done(true)
            .chunkId(targetEvent.getChunkId().toHexString())
            .newVersion(newVersion)
            .doneAt(now)
            .lockExpiresAt(rulebook.getLockExpiresAt())
            .build();
    }

    @Transactional
    public UndoOrRedoActionResponseDto redoAction(ObjectId rulebookId, ObjectId userId, CommitEditDeltaOrDoActionRequestDto request){
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();

        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "redo");

        if (rulebook.getRedoStack().isEmpty()) {
            throw new NoActionsToRedoException(rulebookId);
        }

        Long targetVersion = rulebookRepository.atomicPopRedoAndPushUndo(rulebookId, userId);
        long newVersion = rulebook.getVersion();

        EditEvent targetEvent = editEventRepository.findByRulebookIdAndVersionPostEdit(rulebookId, targetVersion)
            .orElseThrow(
                () -> new IllegalStateException("Database corruption. redoStack pointed to a version that does not exist in the EDIT_EVENT ledger")
            );

        String broadcastEventType = "";
        EditType inverseEditType;
        String eventNewContent = null;
        String eventPreviousContent = null;

        Integer eventIndex = null;
        int actualRestoredIndex = -1;

        switch (targetEvent.getEditType()) {
            case EditType.INSERT:
                rulebookTextRepository.atomicDeleteChunk(rulebookId, targetEvent.getChunkId());
                broadcastEventType = "CHUNK_DELETED";
                inverseEditType = EditType.DELETE;
                eventPreviousContent = targetEvent.getNewContent();

                eventIndex = targetEvent.getIndex();
                break;
            case EditType.DELETE:
                int targetIndex = targetEvent.getIndex();

                RulebookText updatedDocument = rulebookTextRepository.atomicInsertChunk(rulebookId, targetEvent.getPreviousContent(), targetIndex);

                if (updatedDocument == null) {
                    throw new ConcurrentModificationAnomalyException("Failed to insert chunk.");
                }

                actualRestoredIndex = updatedDocument.getIndex();

                broadcastEventType = "CHUNK_INSERTED";
                inverseEditType = EditType.INSERT;
                eventNewContent = targetEvent.getPreviousContent();

                eventIndex = actualRestoredIndex;
                break;
            case EditType.UPDATE:
                rulebookTextRepository.atomicUpdateChunk(rulebookId, targetEvent.getChunkId(), targetEvent.getPreviousContent());
                broadcastEventType = "DELTA_COMMITTED";
                inverseEditType = EditType.UPDATE;
                eventNewContent = targetEvent.getPreviousContent();
                eventPreviousContent = targetEvent.getNewContent();

                eventIndex = targetEvent.getIndex();
                break;
            default:
                throw new IllegalArgumentException(targetEvent.getEditType() + " is not a valid edit type.");
        }

        EditEvent event = EditEvent.builder()
                .rulebookId(rulebookId)
                .editorId(new ObjectId(user.getId()))
                .chunkId(targetEvent.getChunkId())
                .chunkBefore(targetEvent.getChunkBefore())
                .editType(inverseEditType)
                .previousContent(eventPreviousContent)
                .newContent(eventNewContent)
                .index(eventIndex)
                .versionPostEdit(newVersion)
                .compensatesVersion(targetVersion)
                .committedAt(now)
                .build();
        editEventRepository.save(event);

        switch (broadcastEventType) {
            case "CHUNK_INSERTED":
                eventPublisher.publishEvent(ChunkInsertedEventDto.builder()
                        .eventType("CHUNK_INSERTED")
                        .rulebookId(rulebookId.toHexString())
                        .editorId(userId.toHexString())
                        .version(newVersion)
                        .timestamp(now)
                        .chunkId(targetEvent.getChunkId().toHexString())
                        .content(eventNewContent)
                        .index(actualRestoredIndex)
                        .build());
                break;
            case "CHUNK_DELETED":
                eventPublisher.publishEvent(ChunkDeletedEventDto.builder()
                        .eventType("CHUNK_DELETED")
                        .rulebookId(rulebookId.toHexString())
                        .editorId(userId.toHexString())
                        .version(newVersion)
                        .timestamp(now)
                        .chunkId(targetEvent.getChunkId().toHexString())
                        .build());
                break;
            case "DELTA_COMMITTED":
                eventPublisher.publishEvent(DeltaCommitedEventDto.builder()
                        .eventType("DELTA_COMMITTED")
                        .rulebookId(rulebookId.toHexString())
                        .editorId(userId.toHexString())
                        .timestamp(now)
                        .chunkId(targetEvent.getChunkId().toHexString())
                        .deltaContent(eventNewContent)
                        .version(newVersion)
                        .build());
                break;
        }

        return UndoOrRedoActionResponseDto.builder()
                .done(true)
                .chunkId(targetEvent.getChunkId().toHexString())
                .newVersion(newVersion)
                .doneAt(now)
                .lockExpiresAt(rulebook.getLockExpiresAt())
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

    private Rulebook validateRulebookAndLockPossession(ObjectId rulebookId, ObjectId userId , Instant now, VaultBaseRequestDto request, String action){
        Rulebook rulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId,
                request.getExpectedVersion(), now.plusSeconds(LOCK_TIMEOUT_MINUTES * 60));
        if (rulebook == null) {
            // Determine failure reason and throw appropriate error.
            Rulebook currentRulebook = findRulebookOrThrow(rulebookId);

            if (currentRulebook.getLockHeldBy() == null || !currentRulebook.getLockHeldBy().equals(userId)) {
                throw new LockNotHeldException(userId);
            }

            if (currentRulebook.getVersion() != request.getExpectedVersion()) {
                throw new VersionMismatchException(request.getExpectedVersion(), currentRulebook.getVersion());
            }

            throw new ConcurrentModificationAnomalyException("Failed "+ action +" action due to concurrent state modification.");
        }
        return rulebook;
    }
}
