package com.boardwise.backend.vault.repository.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.boardwise.backend.vault.VaultIntegrationTest;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.RulebookRepository;

public class LockRepositoryIntegrationTest extends VaultIntegrationTest {
    @Autowired
    private RulebookRepository rulebookRepository;

    @Nested
    class AtomicAcquireLockOperations{
        private Rulebook emptyRulebook;
        
        @BeforeEach
        void setup(){
            emptyRulebook = rulebookRepository.save(Rulebook.builder().build());
        }

        @Test
        void atomicAcquireWriteLockShouldSucceedWhenLockIsNotHeld() {
            // Arrange
            ObjectId currUser = new ObjectId();
            Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

            // Act
            Rulebook updated = rulebookRepository.atomicAcquireWriteLock(emptyRulebook.getId(), currUser, expiresAt);

            // Assert
            assertEquals(currUser, updated.getLockHeldBy(), "The lock should be held by the current user");
            assertEquals(expiresAt, updated.getLockExpiresAt(), "The expiration time must match exactly");
        }

        @Test
        void atomicAcquireWriteLockShouldSucceedWhenLockIsExpired(){
            // Arrange
            Instant expiredTime = Instant.now().minusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
            Rulebook lockExpiredRulebook = rulebookRepository.save(Rulebook.builder().lockHeldBy(new ObjectId()).lockExpiresAt(expiredTime).build());
            ObjectId currUser = new ObjectId();
            Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

            // Act
            Rulebook updated = rulebookRepository.atomicAcquireWriteLock(lockExpiredRulebook.getId(), currUser, expiresAt);

            // Assert
            assertEquals(currUser, updated.getLockHeldBy(), "The lock should be held by the current user");
            assertEquals(expiresAt, updated.getLockExpiresAt(), "The expiration time must match exactly");
        }

        @Test
        void atomicAcquireWriteLockShouldFailWhenLockIsAlreadyHeldAndNotExpired(){
            // Arrange
            Instant notExpired = Instant.now().plusSeconds(30).truncatedTo(ChronoUnit.MILLIS);
            ObjectId holdingLock = new ObjectId();
            Rulebook lockedRulebook = rulebookRepository.save(Rulebook.builder().lockHeldBy(holdingLock).lockExpiresAt(notExpired).build());
            ObjectId wantsLock = new ObjectId();
            Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
            
            // Act
            Rulebook isNull = rulebookRepository.atomicAcquireWriteLock(lockedRulebook.getId(), wantsLock, expiresAt);
            Rulebook fetched = rulebookRepository.findById(lockedRulebook.getId()).orElse(null);

            // Assert
            assertNull(isNull, "The returned rulebook should be null");
            assertEquals(holdingLock, fetched.getLockHeldBy(), "The owner of the lock should remain unchanged");
            assertEquals(notExpired, fetched.getLockExpiresAt(), "The time of lock expiry should remain unchanged");
        }
    }

    @Nested
    class AtomicValidateAndExtendLockOperations{
        private Rulebook rulebook;
        private ObjectId rulebookId;
        private ObjectId userId;
        private long expectedVersion;

        @BeforeEach
        void setup(){
            rulebookId = new ObjectId();
            userId = new ObjectId();
            expectedVersion = 5;
            rulebook = Rulebook.builder().id(rulebookId).lockHeldBy(userId).version(expectedVersion)
                    .lockExpiresAt(Instant.now().plusSeconds(30).truncatedTo(
                            ChronoUnit.MILLIS))
                    .build();
            rulebookRepository.save(rulebook);
        }
        @Test
        void atomicValidateAndExtendLockShouldSucceedWhenGivenValidLockAndVersion() {
            // Arrange
            Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

            // Act
            Rulebook vaildRulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId, expectedVersion,
                    expiresAt);

            // Assert
            assertEquals(userId, vaildRulebook.getLockHeldBy(), "The lock should be held by the current user");
            assertEquals(expiresAt, vaildRulebook.getLockExpiresAt(), "The expiration time must match exactly");
            assertEquals(expectedVersion + 1, vaildRulebook.getVersion(), "The version must match exactly");
        }

        @Test
        void atomicValidateAndExtendLockShouldFailWhenGivenAnInvalidVersion() {
            // Arrange
            Instant oldExpiresAt = rulebook.getLockExpiresAt();
            Instant newExpiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

            // Act
            Rulebook invaildRulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId,
                    expectedVersion - 1,
                    newExpiresAt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);
            // Assert
            assertNull(invaildRulebook, "The returned rulebook should be null");
            assertNotNull(fetched);
            assertEquals(expectedVersion, fetched.getVersion(), "The version should remain unchanged");
            assertEquals(oldExpiresAt, fetched.getLockExpiresAt(), "The time of lock expiry should remain unchanged");
            assertNull(fetched.getUpdatedAt(), "The updatedAt should remain unchanged");
        }

        @Test
        void atomicValidateAndExtendLockShouldFailWhenGivenALockGrantedToADifferentUser() {
            // Arrange
            Instant oldExpiresAt = rulebook.getLockExpiresAt();
            Instant newExpiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

            // Act
            Rulebook invaildRulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, new ObjectId(),
                    expectedVersion,
                    newExpiresAt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);
            // Assert
            assertNull(invaildRulebook, "The returned rulebook should be null");
            assertNotNull(fetched);
            assertEquals(expectedVersion, fetched.getVersion(), "The version should remain unchanged");
            assertEquals(oldExpiresAt, fetched.getLockExpiresAt(), "The time of lock expiry should remain unchanged");
            assertNull(fetched.getUpdatedAt(), "The updatedAt should remain unchanged");
        }

        @Test
        void atomicValidateAndExtendLockShouldFailWhenTheRulebookIdIsInvalid(){
            // Arrange
            Instant oldExpiresAt = rulebook.getLockExpiresAt();
            Instant newExpiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

            // Act
            Rulebook invaildRulebook = rulebookRepository.atomicValidateAndExtendLock(new ObjectId(), userId,
                    expectedVersion,
                    newExpiresAt);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);
            // Assert
            assertNull(invaildRulebook, "The returned rulebook should be null");
            assertNotNull(fetched);
            assertEquals(expectedVersion, fetched.getVersion(), "The version should remain unchanged");
            assertEquals(oldExpiresAt, fetched.getLockExpiresAt(), "The time of lock expiry should remain unchanged");
            assertNull(fetched.getUpdatedAt(), "The updatedAt should remain unchanged");
        }
    }

    @Nested
    class AtomicReleaseWriteLockOperations{
        private Rulebook rulebook;
        private ObjectId rulebookId;
        private ObjectId userId;
        private Instant expiresAt;

        @BeforeEach
        void setup(){
            rulebookId = new ObjectId();
            userId = new ObjectId();
            expiresAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            rulebook = Rulebook.builder().id(rulebookId).title("Book0").lockHeldBy(userId).lockExpiresAt(expiresAt).build();
            rulebookRepository.save(rulebook);
        }

        @Test
        void atomicReleaseWriteLockShouldSucceedForValidRulebookIdAndIfLockHeldByCurrentUser(){
            // Act
            Rulebook valid = rulebookRepository.atomicReleaseWriteLock(rulebookId, userId);
            // Assert
            assertNull(valid.getLockHeldBy(), "The lock held by field should be null");
            assertNull(valid.getLockExpiresAt(), "The lock expiry time should be null");
        }
        
        
        @Test
        void atomicReleaseWriteLockShouldFailIfLockHeldByDifferentUser(){
            // Arrange
            ObjectId differentUser = new ObjectId();
            
            // Act
            Rulebook inValid = rulebookRepository.atomicReleaseWriteLock(rulebookId, differentUser);
            Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);
            
            // Assert
            assertNull(inValid, "The returned rulebook should be null");
            assertEquals(userId,fetched.getLockHeldBy(), "The lock held by should remain unchanged");
            assertEquals(expiresAt,fetched.getLockExpiresAt(), "The lock expiry time should remain unchanged");
        }

        @Test
        void atomicReleaseAllWriteLocksShouldSucceedIfUserHoldsTheLocks(){
            // Arrange
            ObjectId iOwn = new ObjectId();
            ObjectId rando = new ObjectId();
            List<Rulebook> someOwnedByCurrent = List.of(
                Rulebook.builder().title("Book1").lockHeldBy(iOwn).lockExpiresAt(expiresAt).build(),
                Rulebook.builder().title("Book2").lockHeldBy(rando).lockExpiresAt(expiresAt).build(),
                Rulebook.builder().title("Book3").lockHeldBy(iOwn).lockExpiresAt(expiresAt).build()
            );
            rulebookRepository.saveAll(someOwnedByCurrent);

            // Act
            rulebookRepository.atomicReleaseAllWriteLocks(iOwn);
            List<Rulebook> fetched = rulebookRepository.findAll();

            // Assert
            assertEquals(userId, fetched.get(0).getLockHeldBy(), "The lock held by should remain unchanged");
            assertNull(fetched.get(1).getLockHeldBy(), "The lock held by should be null");
            assertEquals(rando, fetched.get(2).getLockHeldBy(), "The lock held by should remain unchanged");
            assertNull(fetched.get(3).getLockHeldBy(), "The lock held by should be null");
            assertEquals(expiresAt, fetched.get(0).getLockExpiresAt(), "The lock expires at should remain unchanged");
            assertNull(fetched.get(1).getLockExpiresAt(), "The lock expires at should be null");
            assertEquals(expiresAt, fetched.get(2).getLockExpiresAt(), "The lock expires at should remain unchanged");
            assertNull(fetched.get(3).getLockExpiresAt(), "The lock expires at should be null");
        }

        @Test
        void atomicReleaseAllWriteLocksShouldFailIfDifferentUserHoldsTheLocks(){
            // Arrange
            ObjectId us1 = new ObjectId();
            ObjectId us2 = new ObjectId();
            ObjectId us3 = new ObjectId();
            ObjectId iHoldNothing = new ObjectId();
            List<Rulebook> noneOwnedByCurrent = List.of(
                    Rulebook.builder().title("Book1").lockHeldBy(us1).lockExpiresAt(expiresAt).build(),
                    Rulebook.builder().title("Book2").lockHeldBy(us2).lockExpiresAt(expiresAt).build(),
                    Rulebook.builder().title("Book3").lockHeldBy(us3).lockExpiresAt(expiresAt).build());
            rulebookRepository.saveAll(noneOwnedByCurrent);

            // Act
            rulebookRepository.atomicReleaseAllWriteLocks(iHoldNothing);
            List<Rulebook> fetched = rulebookRepository.findAll();

            // Assert
            assertEquals(userId, fetched.get(0).getLockHeldBy(), "The lock held by should remain unchanged");
            assertEquals(us1,fetched.get(1).getLockHeldBy(), "The lock held by should remain unchanged");
            assertEquals(us2, fetched.get(2).getLockHeldBy(), "The lock held by should remain unchanged");
            assertEquals(us3,fetched.get(3).getLockHeldBy(), "The lock held by should remain unchanged");
            for(int i= 0; i< 4; i++){
                assertEquals(expiresAt, fetched.get(i).getLockExpiresAt(), "The lock expires at should remain unchanged");
            }
        }
    }
}
