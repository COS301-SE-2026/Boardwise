package com.boardwise.backend.vault;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.boardwise.backend.vault.service.LockManagerService;
import com.boardwise.backend.vault.websocket.VaultEventPublisher;

@ExtendWith(MockitoExtension.class)
public class LockManagerServiceTest {

        @Mock
        private WriteLockRepository writeLockRepository;
        @Mock
        private RulebookRepository rulebookRepository;
        @Mock
        private RulebookTextRepository rulebookTextRepository;
        @Mock
        private EditEventRepository editEventRepository;
        @Mock
        private VaultEventPublisher eventPublisher;

        @InjectMocks
        private LockManagerService lockManagerService;

        private ObjectId rulebookId;
        private ObjectId userId;
        private ObjectId otherUserId;
        private Rulebook mockRulebook;
        private RulebookText mockRulebookText;
        private WriteLock activeLock;
        private WriteLock expiredLock;

        @BeforeEach
        void setUp() {
                rulebookId = new ObjectId();
                userId = new ObjectId();
                otherUserId = new ObjectId();

                mockRulebook = Rulebook.builder()
                                .id(rulebookId)
                                .gameName("Catan")
                                .edition("3rd Edition")
                                .status("Ready")
                                .version(3)
                                .contributorId(new ObjectId())
                                .r2PdfKey("rulebooks/catan.pdf")
                                .uploadedAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                mockRulebookText = RulebookText.builder()
                                .id(new ObjectId())
                                .rulebookId(rulebookId)
                                .content("Original content.")
                                .version(3)
                                .updatedAt(Instant.now())
                                .build();

                activeLock = WriteLock.builder()
                                .id(new ObjectId())
                                .rulebookId(rulebookId)
                                .heldByUserId(userId)
                                .acquiredAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(30))
                                .build();

                expiredLock = WriteLock.builder()
                                .id(new ObjectId())
                                .rulebookId(rulebookId)
                                .heldByUserId(otherUserId)
                                .acquiredAt(Instant.now().minusSeconds(60))
                                .expiresAt(Instant.now().minusSeconds(30))
                                .build();
        }

        // -------------------------------------------------------------------------
        // acquireLock
        // -------------------------------------------------------------------------

        @Test
        void acquireLock_grantsLockWhenNoneHeld() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());
                when(writeLockRepository.insert(any(WriteLock.class))).thenAnswer(i -> i.getArgument(0));

                LockResponseDto result = lockManagerService.acquireLock(rulebookId, userId);

                assertTrue(result.isLockGranted());
                assertEquals(userId.toHexString(), result.getLockedBy());
                assertNotNull(result.getExpiresAt());
                assertEquals(3, result.getCurrentVersion());
                verify(writeLockRepository).insert(any(WriteLock.class));
        }

        @Test
        void acquireLock_throwsConflictWhenActiveLockExists() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));

                assertThrows(LockConflictException.class,
                        () -> lockManagerService.acquireLock(rulebookId, userId));

                verify(writeLockRepository, never()).insert(any(WriteLock.class));
        }

        @Test
        void acquireLock_clearsExpiredLockAndGrantsNew() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(expiredLock));
                when(writeLockRepository.insert(any(WriteLock.class))).thenAnswer(i -> i.getArgument(0));

                LockResponseDto result = lockManagerService.acquireLock(rulebookId, userId);

                assertTrue(result.isLockGranted());
                verify(writeLockRepository).delete(expiredLock);
                verify(writeLockRepository).insert(any(WriteLock.class));
        }

        @Test
        void acquireLock_throwsNotFoundForUnknownRulebook() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> lockManagerService.acquireLock(rulebookId, userId));
        }

        // -------------------------------------------------------------------------
        // releaseLock
        // -------------------------------------------------------------------------

        @Test
        void releaseLock_successfullyReleasesLock() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));

                lockManagerService.releaseLock(rulebookId, userId);

                verify(writeLockRepository).delete(activeLock);
        }

        @Test
        void releaseLock_throwsForbiddenWhenCallerIsNotLockHolder() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));

                assertThrows(LockNotHeldException.class,
                                () -> lockManagerService.releaseLock(rulebookId, otherUserId));

                verify(writeLockRepository, never()).delete(any());
        }

        @Test
        void releaseLock_throwsForbiddenWhenNoLockExists() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());

                assertThrows(LockNotHeldException.class,
                                () -> lockManagerService.releaseLock(rulebookId, userId));
        }

        @Test
        void releaseLock_throwsNotFoundForUnknownRulebook() {
                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> lockManagerService.releaseLock(rulebookId, userId));
        }

        // -------------------------------------------------------------------------
        // commitDelta
        // -------------------------------------------------------------------------

        @Test
        void commitDelta_successfullyCommitsDelta() {
                CommitDeltaRequestDto request = new CommitDeltaRequestDto();
                request.setExpectedVersion(3);
                request.setDelta("Updated content.");

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));
                when(rulebookTextRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockRulebookText));
                when(rulebookTextRepository.save(any(RulebookText.class))).thenAnswer(i -> i.getArgument(0));
                when(rulebookRepository.save(any(Rulebook.class))).thenAnswer(i -> i.getArgument(0));
                when(editEventRepository.save(any(EditEvent.class))).thenAnswer(i -> i.getArgument(0));
                when(writeLockRepository.save(any(WriteLock.class))).thenAnswer(i -> i.getArgument(0));

                CommitDeltaResponseDto result = lockManagerService.commitDelta(rulebookId, userId, request);

                assertTrue(result.isCommitted());
                assertEquals(4, result.getNewVersion());
                assertNotNull(result.getCommittedAt());
                verify(rulebookTextRepository).save(any(RulebookText.class));
                verify(rulebookRepository).save(any(Rulebook.class));
                verify(editEventRepository).save(any(EditEvent.class));
                verify(writeLockRepository).save(any(WriteLock.class));
        }

        @Test
        void commitDelta_throwsForbiddenWhenCallerIsNotLockHolder() {
                CommitDeltaRequestDto request = new CommitDeltaRequestDto();
                request.setExpectedVersion(3);
                request.setDelta("Updated content.");

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));

                assertThrows(LockNotHeldException.class,
                                () -> lockManagerService.commitDelta(rulebookId, otherUserId, request));

                verify(rulebookTextRepository, never()).save(any());
                verify(editEventRepository, never()).save(any());
        }

        @Test
        void commitDelta_throwsForbiddenWhenNoLockHeld() {
                CommitDeltaRequestDto request = new CommitDeltaRequestDto();
                request.setExpectedVersion(3);
                request.setDelta("Updated content.");

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.empty());

                assertThrows(LockNotHeldException.class,
                                () -> lockManagerService.commitDelta(rulebookId, userId, request));

                verify(rulebookTextRepository, never()).save(any());
                verify(editEventRepository, never()).save(any());
        }

        @Test
        void commitDelta_throwsConflictOnVersionMismatch() {
                CommitDeltaRequestDto request = new CommitDeltaRequestDto();
                request.setExpectedVersion(1);
                request.setDelta("Updated content.");

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));
                when(rulebookTextRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockRulebookText));

                assertThrows(VersionMismatchException.class,
                                () -> lockManagerService.commitDelta(rulebookId, userId, request));

                verify(rulebookTextRepository, never()).save(any());
                verify(editEventRepository, never()).save(any());
        }

        @Test
        void commitDelta_throwsNotFoundWhenRulebookMissing() {
                CommitDeltaRequestDto request = new CommitDeltaRequestDto();
                request.setExpectedVersion(3);
                request.setDelta("Updated content.");

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.empty());

                assertThrows(RulebookNotFoundException.class,
                                () -> lockManagerService.commitDelta(rulebookId, userId, request));
        }

        @Test
        void commitDelta_refreshesLockExpiryOnSuccess() {
                CommitDeltaRequestDto request = new CommitDeltaRequestDto();
                request.setExpectedVersion(3);
                request.setDelta("Updated content.");

                Instant beforeCommit = Instant.now();

                when(rulebookRepository.findById(rulebookId)).thenReturn(Optional.of(mockRulebook));
                when(writeLockRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(activeLock));
                when(rulebookTextRepository.findByRulebookId(rulebookId)).thenReturn(Optional.of(mockRulebookText));
                when(rulebookTextRepository.save(any(RulebookText.class))).thenAnswer(i -> i.getArgument(0));
                when(rulebookRepository.save(any(Rulebook.class))).thenAnswer(i -> i.getArgument(0));
                when(editEventRepository.save(any(EditEvent.class))).thenAnswer(i -> i.getArgument(0));
                when(writeLockRepository.save(any(WriteLock.class))).thenAnswer(i -> i.getArgument(0));

                lockManagerService.commitDelta(rulebookId, userId, request);

                assertTrue(activeLock.getExpiresAt().isAfter(beforeCommit.plusSeconds(29)));
                verify(writeLockRepository, times(1)).save(activeLock);
        }
}