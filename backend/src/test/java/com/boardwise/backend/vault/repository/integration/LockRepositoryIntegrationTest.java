package com.boardwise.backend.vault.repository.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.boardwise.backend.vault.VaultIntegrationTest;
import com.boardwise.backend.vault.exception.RulebookNotFoundException;
import com.boardwise.backend.vault.model.Rulebook;
import com.boardwise.backend.vault.repository.RulebookRepository;

public class LockRepositoryIntegrationTest extends VaultIntegrationTest {
    @Autowired
    private RulebookRepository rulebookRepository;

    @Test
    void atomicAcquireWriteLockShouldSucceedWhenLockIsNotHeld(){
        // Arrange
        Rulebook emptyRulebook = Rulebook.builder().build();
        rulebookRepository.save(emptyRulebook);
        
        ObjectId currUser = new ObjectId();
        Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);

        // Act
        Rulebook updated = rulebookRepository.atomicAcquireWriteLock(emptyRulebook.getId(), currUser, expiresAt);
        
        // Assert
        assertEquals(currUser, updated.getLockHeldBy(), "The lock should be held by the current user");
        assertEquals(expiresAt, updated.getLockExpiresAt(), "The expiration time must match exactly");
    }

    @Test
    void atomicValidateAndExtendLockShouldSucceedWhenGivenValidLockAndVersion(){
        // Arrange
        ObjectId rulebookId = new ObjectId();
        ObjectId userId = new ObjectId();
        long expectedVersion = 1;
        Rulebook rulebook = Rulebook.builder().id(rulebookId).lockHeldBy(userId).version(expectedVersion).lockExpiresAt(Instant.now().plusSeconds(30).truncatedTo(
                ChronoUnit.MILLIS)).build();
        rulebookRepository.save(rulebook);
        Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
        
        // Act
        Rulebook vaildRulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId, expectedVersion, expiresAt);
        
        // Assert
        assertEquals(userId, vaildRulebook.getLockHeldBy(), "The lock should be held by the current user");
        assertEquals(expiresAt, vaildRulebook.getLockExpiresAt(), "The expiration time must match exactly");
        assertEquals(expectedVersion + 1, vaildRulebook.getVersion(), "The version must match exactly");
    }
    
    @Test
    void atomicValidateAndExtendLockShouldFailWhenGivenAnInvalidVersion(){
        // Arrange
        ObjectId rulebookId = new ObjectId();
        ObjectId userId = new ObjectId();
        long expectedVersion = 5;
        Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
        Rulebook rulebook = Rulebook.builder().id(rulebookId).lockHeldBy(userId).version(expectedVersion)
                .lockExpiresAt(expiresAt)
                .updatedAt(null)
                .build();
        rulebookRepository.save(rulebook);

        // Act
        Rulebook invaildRulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, userId, expectedVersion-1,
                expiresAt);
        Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);
        // Assert
        assertNull(invaildRulebook, "The returned rulebook should be null");
        assertNotNull(fetched);
        assertEquals(expectedVersion, fetched.getVersion(), "The version should remain unchanged");
        assertEquals(expiresAt, fetched.getLockExpiresAt(), "The time of lock expiry should remain unchanged");
        assertNull(fetched.getUpdatedAt(), "The updatedAt should remain unchanged");
    }
    
    @Test
    void atomicValidateAndExtendLockShouldFailWhenGivenALockGrantedToADifferentUser(){
        // Arrange
        ObjectId rulebookId = new ObjectId();
        ObjectId userId = new ObjectId();
        long expectedVersion = 5;
        Instant expiresAt = Instant.now().plusSeconds(60).truncatedTo(ChronoUnit.MILLIS);
        Rulebook rulebook = Rulebook.builder().id(rulebookId).lockHeldBy(userId).version(expectedVersion)
                .lockExpiresAt(expiresAt)
                .updatedAt(null)
                .build();
        rulebookRepository.save(rulebook);

        // Act
        Rulebook invaildRulebook = rulebookRepository.atomicValidateAndExtendLock(rulebookId, new ObjectId(),
                expectedVersion,
                expiresAt);
        Rulebook fetched = rulebookRepository.findById(rulebookId).orElse(null);
        // Assert
        assertNull(invaildRulebook, "The returned rulebook should be null");
        assertNotNull(fetched);
        assertEquals(expectedVersion, fetched.getVersion(), "The version should remain unchanged");
        assertEquals(expiresAt, fetched.getLockExpiresAt(), "The time of lock expiry should remain unchanged");
        assertNull(fetched.getUpdatedAt(), "The updatedAt should remain unchanged");
    }
}
