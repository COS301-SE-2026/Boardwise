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
    public CommitEditDeltaResponseDto commitEditDelta(ObjectId rulebookId, ObjectId userId, CommitEditDeltaOrDoActionRequestDto request){
        // Check if user exists
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();
        // Attempt commit edit delta
        // 1. Update RULEBOOK
        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "edit");

        long nextVersion = rulebook.getVersion();

        ObjectId targetChunkId = new ObjectId(request.getChunkId());
        
        // 2. Fetch Previous Text State
        RulebookText chunkBeforeUpdate = rulebookTextRepository.findBySpecificChunk(rulebookId, targetChunkId)
            .orElseThrow(() -> new ChunkNotFoundException(rulebookId, targetChunkId));
        String previousText = chunkBeforeUpdate.getChunks().get(0).getContent();

        // 3. Update RULEBOOK_TEXT
        rulebookTextRepository.atomicUpdateChunk(rulebookId, new ObjectId(request.getChunkId()), request.getContent());

        // Push onto undoStack and clear redoStack
        rulebookRepository.atomicCommitForwardEdit(rulebookId, nextVersion);
        
        // 4. Insert EDIT_EVENT
        EditEvent event = EditEvent.builder()
            .rulebookId(rulebookId)
            .editorId(new ObjectId(user.getId()))
            .chunkId(new ObjectId(request.getChunkId()))
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
        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "insert");

        long nextVersion = rulebook.getVersion();

        // 2. Update RULEBOOK_TEXT
        ObjectId chunkId = new ObjectId();
        RulebookText updatedDocument = rulebookTextRepository.atomicInsertChunk(rulebookId, chunkId, request.getContent(),
                request.getInsertIndex());

        if(updatedDocument == null){
            throw new ConcurrentModificationAnomalyException("Failed to insert chunk.");
        }

        int actualAssignedIndex = updatedDocument.getChunks().stream()
            .filter(c -> c.getChunkId().equals(chunkId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Chunk missing after atomic insert"))
            .getIndex();

        // Push onto undoStack and clear redoStack
        rulebookRepository.atomicCommitForwardEdit(rulebookId, nextVersion);

        // 3. Insert EDIT_EVENT
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
        // Validate user
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();

        // Attempt insert chunk
        // 1. Update RULEBOOK
        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "delete");

        long nextVersion = rulebook.getVersion();
        
        // 2. Update RULEBOOK_TEXT
        ObjectId chunkToDeleteId = new ObjectId(request.getChunkId());
        RulebookText chunkBeforeDelete = rulebookTextRepository.findBySpecificChunk(rulebookId, chunkToDeleteId)
            .orElseThrow(() -> new ChunkNotFoundException(rulebookId, chunkToDeleteId));
        String actualPreviousText = chunkBeforeDelete.getChunks().get(0).getContent();
        int actualPreviousIndex = chunkBeforeDelete.getChunks().get(0).getIndex();
        boolean deleted = rulebookTextRepository.atomicDeleteChunk(rulebookId, chunkToDeleteId);

        // Push onto undoStack and clear redoStack
        rulebookRepository.atomicCommitForwardEdit(rulebookId, nextVersion);
        
        // 3. Insert EDIT_EVENT
        EditEvent event = EditEvent.builder()
                .rulebookId(rulebookId)
                .editorId(new ObjectId(user.getId()))
                .chunkId(chunkToDeleteId)
                .chunkBefore(request.getChunkBeforeId() != null
                    ? new ObjectId(request.getChunkBeforeId())
                    : null)
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
        // 1. Validation and Session
        // Validate user
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();
        // Fetch Rulebook and validate lock
        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "undo");
        // Evaluate Undo stack
        if(rulebook.getUndoStack().isEmpty()){
            throw new NoActionsToUndoException(rulebookId);
        }
        // Atomic Pointer Update
        Long targetVersion = rulebookRepository.atomicPopUndoAndPushRedo(rulebookId, userId);
        long newVersion = rulebook.getVersion();

        // 2. Retrieval
        EditEvent targetEvent = editEventRepository.findByRulebookIdAndVersionPostEdit(rulebookId, targetVersion).orElseThrow(
           () -> new IllegalStateException("Database corruption. undoStack pointed to a version that does not exist in the EDIT_EVENT ledger")
        );

        // 3. Execution
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

                RulebookText updatedDocument = rulebookTextRepository.atomicInsertChunk(rulebookId,
                        targetEvent.getChunkId(), targetEvent.getPreviousContent(), targetIndex);

                if (updatedDocument == null) {
                    throw new ConcurrentModificationAnomalyException("Failed to insert chunk.");
                }

                actualRestoredIndex = updatedDocument.getChunks().stream()
                        .filter(c -> c.getChunkId().equals(targetEvent.getChunkId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Chunk missing after undo insert"))
                        .getIndex();

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

        // 4. Insert Edit_Event
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

        // 5. Broadcast and Return
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
            .build();
    }

    @Transactional
    public UndoOrRedoActionResponseDto redoAction(ObjectId rulebookId, ObjectId userId, CommitEditDeltaOrDoActionRequestDto request){
        // 1. Validation and Session
        // Validate user
        User user = findUserOrThrow(userId);
        Instant now = Instant.now();
        // Fetch Rulebook and validate lock
        Rulebook rulebook = validateRulebookAndLockPossession(rulebookId, userId, now, request, "redo");
        // Evaluate Redo stack
        if (rulebook.getRedoStack().isEmpty()) {
            throw new NoActionsToRedoException(rulebookId);
        }
        // Atomic Pointer Update
        Long targetVersion = rulebookRepository.atomicPopRedoAndPushUndo(rulebookId, userId);
        long newVersion = rulebook.getVersion();

        // 2. Retrieval
        EditEvent targetEvent = editEventRepository.findByRulebookIdAndVersionPostEdit(rulebookId, targetVersion)
                .orElseThrow(
                        () -> new IllegalStateException(
                                "Database corruption. redoStack pointed to a version that does not exist in the EDIT_EVENT ledger"));

        // 3. Execution
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

                RulebookText updatedDocument = rulebookTextRepository.atomicInsertChunk(rulebookId,
                        targetEvent.getChunkId(), targetEvent.getPreviousContent(), targetIndex);

                if (updatedDocument == null) {
                    throw new ConcurrentModificationAnomalyException("Failed to insert chunk.");
                }

                actualRestoredIndex = updatedDocument.getChunks().stream()
                        .filter(c -> c.getChunkId().equals(targetEvent.getChunkId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Chunk missing after undo insert"))
                        .getIndex();

                broadcastEventType = "CHUNK_INSERTED";
                inverseEditType = EditType.INSERT;
                eventNewContent = targetEvent.getPreviousContent();

                eventIndex = actualRestoredIndex;
                break;
            case EditType.UPDATE:
                rulebookTextRepository.atomicUpdateChunk(rulebookId, targetEvent.getChunkId(),
                        targetEvent.getPreviousContent());
                broadcastEventType = "DELTA_COMMITTED";
                inverseEditType = EditType.UPDATE;
                eventNewContent = targetEvent.getPreviousContent();
                eventPreviousContent = targetEvent.getNewContent();

                eventIndex = targetEvent.getIndex();
                break;
            default:
                throw new IllegalArgumentException(targetEvent.getEditType() + " is not a valid edit type.");
        }

        // 4. Insert Edit_Event
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

        // 5. Broadcast and Return
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
                    "Failed "+ action +" action due to concurrent state modification.");
        }
        return rulebook;
    }
}
