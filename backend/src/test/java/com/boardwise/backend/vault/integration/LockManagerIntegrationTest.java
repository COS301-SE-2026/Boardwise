package com.boardwise.backend.vault.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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

@SpringBootTest(properties = {
        "r2.access-key=test-access-key",
        "r2.secret-key=test-secret-key",
        "r2.account-id=test-account-id",
        "r2.bucket-listings=test-listings-bucket",
        "r2.bucket-profiles=test-profiles-bucket",
        "r2.dev-url=http://localhost:9000", // ADDED: resolves ${r2.dev-url}
        "jwt.secret=test-secret-key-that-is-long-enough-for-hmac"
})
@Testcontainers
public class LockManagerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

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

    @BeforeEach
    void setUp() {
        writeLockRepository.deleteAll();
        editEventRepository.deleteAll();
        rulebookTextRepository.deleteAll();
        rulebookRepository.deleteAll();

        rulebookId = new ObjectId();
        userId1 = new ObjectId();
        userId2 = new ObjectId();

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

        rulebookTextRepository.save(RulebookText.builder()
                .id(new ObjectId())
                .rulebookId(rulebookId)
                .content("Original content.")
                .version(1)
                .updatedAt(Instant.now())
                .build());
    }

    // -------------------------------------------------------------------------
    // Concurrent lock requests — only one must be granted
    // -------------------------------------------------------------------------

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

        // Exactly one thread wins the lock
        assertEquals(1, granted);
        // Exactly one lock document persisted
        assertEquals(1, writeLockRepository.findAll().size());
    }

    // -------------------------------------------------------------------------
    // Voluntary lock release
    // -------------------------------------------------------------------------

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

        // Lock must still be present
        assertTrue(writeLockRepository.findByRulebookId(rulebookId).isPresent());
    }

    // -------------------------------------------------------------------------
    // Expired lock — can be reclaimed
    // -------------------------------------------------------------------------

    @Test
    void expiredLock_canBeReclaimedByAnotherUser() {
        lockManagerService.acquireLock(rulebookId, userId1);

        // Backdate the expiry to simulate timeout
        WriteLock lock = writeLockRepository.findByRulebookId(rulebookId).get();
        lock.setExpiresAt(Instant.now().minusSeconds(1));
        writeLockRepository.save(lock);

        // userId2 should now be able to acquire the expired lock
        LockResponseDto response = lockManagerService.acquireLock(rulebookId, userId2);

        assertTrue(response.isLockGranted());
        assertEquals(userId2.toHexString(), response.getLockedBy());

        // New lock belongs to userId2
        WriteLock newLock = writeLockRepository.findByRulebookId(rulebookId).get();
        assertEquals(userId2, newLock.getHeldByUserId());
    }

    // -------------------------------------------------------------------------
    // Version mismatch — no data corruption
    // -------------------------------------------------------------------------

    @Test
    void commitDelta_versionMismatch_noDataCorruption() {
        lockManagerService.acquireLock(rulebookId, userId1);

        CommitDeltaRequestDto request = new CommitDeltaRequestDto();
        request.setExpectedVersion(99); // wrong — actual is 1
        request.setDelta("This should not be committed.");

        assertThrows(VersionMismatchException.class,
                () -> lockManagerService.commitDelta(rulebookId, userId1, request));

        // No edit event appended
        List<EditEvent> events = editEventRepository
                .findByRulebookIdOrderByCommittedAtAsc(rulebookId);
        assertTrue(events.isEmpty());

        // Rulebook version unchanged
        Rulebook rulebook = rulebookRepository.findById(rulebookId).get();
        assertEquals(1, rulebook.getVersion());

        // Text content and version unchanged
        RulebookText text = rulebookTextRepository.findByRulebookId(rulebookId).get();
        assertEquals("Original content.", text.getContent());
        assertEquals(1, text.getVersion());
    }

    // -------------------------------------------------------------------------
    // Successful commit — version incremented, edit event persisted
    // -------------------------------------------------------------------------

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

        // Exactly one edit event with the correct payload
        List<EditEvent> events = editEventRepository
                .findByRulebookIdOrderByCommittedAtAsc(rulebookId);
        assertEquals(1, events.size());
        assertEquals("Updated content.", events.get(0).getDelta());
        assertEquals(2, events.get(0).getVersionAfter());

        // Rulebook version incremented
        Rulebook rulebook = rulebookRepository.findById(rulebookId).get();
        assertEquals(2, rulebook.getVersion());
    }
}