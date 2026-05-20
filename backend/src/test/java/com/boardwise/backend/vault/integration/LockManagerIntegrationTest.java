package com.boardwise.backend.vault.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.boardwise.backend.vault.dto.request.CommitDeltaRequestDto;
import com.boardwise.backend.vault.dto.response.CommitDeltaResponseDto;
import com.boardwise.backend.vault.dto.response.LockResponseDto;
import com.boardwise.backend.vault.exception.LockConflictException;
import com.boardwise.backend.vault.exception.LockNotHeldException;
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
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class LockManagerIntegrationTest {

    @Autowired
    private LockManagerService lockManagerService;
    @Autowired
    private RulebookRepository rulebookRepository;
    @Autowired
    private RulebookTextRepository rulebookTextRepository;
    @Autowired
    private WriteLockRepository writeLockRepository;
    @Autowired
    private EditEventRepository editEventRepository;

    private ObjectId rulebookId;
    private ObjectId userId1;
    private ObjectId userId2;

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @BeforeEach
    void setUp() {
        // clean state before each test
        writeLockRepository.deleteAll();
        editEventRepository.deleteAll();
        rulebookTextRepository.deleteAll();
        rulebookRepository.deleteAll();

        rulebookId = new ObjectId();
        userId1 = new ObjectId();
        userId2 = new ObjectId();

        // seed a rulebook
        rulebookRepository.save(Rulebook.builder()
                .id(rulebookId)
                .gameName("Catan")
                .edition("3rd Edition")
                .status("Ready")
                .version(1)
                .contributorId(userId1)
                .r2PdfKey("rulebooks/catan.pdf")
                .uploadedAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        // seed rulebook text
        rulebookTextRepository.save(RulebookText.builder()
                .id(new ObjectId())
                .rulebookId(rulebookId)
                .content("Original content.")
                .version(1)
                .updatedAt(Instant.now())
                .build());
    }

    // --- concurrent lock requests ---

    @Test
    void concurrentLockRequests_onlyOneGranted() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            ObjectId userId = i == 0 ? userId1 : userId2;
            futures.add(executor.submit(() -> {
                latch.await();
                try {
                    lockManagerService.acquireLock(rulebookId, userId);
                    return true;
                } catch (LockConflictException e) {
                    return false;
                }
            }));
        }

        latch.countDown();
        executor.shutdown();

        long granted = futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(Boolean::booleanValue)
                .count();

        // exactly one lock granted
        assertEquals(1, granted);

        // exactly one lock document in DB
        assertEquals(1, writeLockRepository.findAll().size());
    }

    // --- voluntary lock release ---

    @Test
    void releaseLock_clearsLockDocument() {
        lockManagerService.acquireLock(rulebookId, userId1);
        assertTrue(writeLockRepository.findByRulebookId(rulebookId).isPresent());

        lockManagerService.releaseLock(rulebookId, userId1);
        assertFalse(writeLockRepository.findByRulebookId(rulebookId).isPresent());
    }

    @Test
    void releaseLock_throwsForbiddenForNonHolder() {
        lockManagerService.acquireLock(rulebookId, userId1);

        assertThrows(LockNotHeldException.class,
                () -> lockManagerService.releaseLock(rulebookId, userId2));

        // lock still exists
        assertTrue(writeLockRepository.findByRulebookId(rulebookId).isPresent());
    }

    // --- idle lock expiry ---

    @Test
    void expiredLock_isNotGrantable_untilExpired() throws InterruptedException {
        // acquire lock with userId1
        lockManagerService.acquireLock(rulebookId, userId1);

        // manually backdate the lock expiry to simulate expiry
        WriteLock lock = writeLockRepository.findByRulebookId(rulebookId).get();
        lock.setExpiresAt(Instant.now().minusSeconds(1));
        writeLockRepository.save(lock);

        // userId2 should now be able to acquire since lock is expired
        LockResponseDto response = lockManagerService.acquireLock(rulebookId, userId2);

        assertTrue(response.isLockGranted());
        assertEquals(userId2.toHexString(), response.getLockedBy());

        // old lock cleared, new lock in DB
        WriteLock newLock = writeLockRepository.findByRulebookId(rulebookId).get();
        assertEquals(userId2, newLock.getHeldByUserId());
    }

    // --- version mismatch ---

    @Test
    void commitDelta_versionMismatch_noDataCorruption() {
        lockManagerService.acquireLock(rulebookId, userId1);

        CommitDeltaRequestDto request = new CommitDeltaRequestDto();
        request.setExpectedVersion(99); // wrong version
        request.setDelta("This should not be committed.");

        assertThrows(VersionMismatchException.class,
                () -> lockManagerService.commitDelta(rulebookId, userId1, request));

        // verify no edit event was appended
        List<EditEvent> events = editEventRepository
                .findByRulebookIdOrderByCommittedAtAsc(rulebookId);
        assertTrue(events.isEmpty());

        // verify version unchanged
        Rulebook rulebook = rulebookRepository.findById(rulebookId).get();
        assertEquals(1, rulebook.getVersion());

        // verify text unchanged
        RulebookText text = rulebookTextRepository
                .findByRulebookId(rulebookId).get();
        assertEquals("Original content.", text.getContent());
        assertEquals(1, text.getVersion());
    }

    // --- successful commit ---

    @Test
    void commitDelta_successfullyCommitsAndIncrementsVersion() {
        lockManagerService.acquireLock(rulebookId, userId1);

        CommitDeltaRequestDto request = new CommitDeltaRequestDto();
        request.setExpectedVersion(1);
        request.setDelta("Updated content.");

        CommitDeltaResponseDto response = lockManagerService
                .commitDelta(rulebookId, userId1, request);

        assertTrue(response.isCommitted());
        assertEquals(2, response.getNewVersion());

        // verify edit event appended
        List<EditEvent> events = editEventRepository
                .findByRulebookIdOrderByCommittedAtAsc(rulebookId);
        assertEquals(1, events.size());
        assertEquals("Updated content.", events.get(0).getDelta());
        assertEquals(2, events.get(0).getVersionAfter());

        // verify rulebook version incremented
        Rulebook rulebook = rulebookRepository.findById(rulebookId).get();
        assertEquals(2, rulebook.getVersion());
    }
}