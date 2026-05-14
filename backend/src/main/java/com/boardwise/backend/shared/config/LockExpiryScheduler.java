package com.boardwise.backend.shared.config;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boardwise.backend.vault.model.WriteLock;
import com.boardwise.backend.vault.repository.WriteLockRepository;
import com.boardwise.backend.vault.websocket.VaultEventPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockExpiryScheduler {
    private static final Logger log = LoggerFactory.getLogger(LockExpiryScheduler.class);
    private final WriteLockRepository writeLockRepository;

    private final VaultEventPublisher eventPublisher;

    @Scheduled(fixedRate = 10000) // runs every 10 seconds
    public void expireStaleLocks(){
        List<WriteLock> expiredLocks = writeLockRepository
            .findByExpiresAtBefore(Instant.now()); // This is more efficient - MongoDB does the filtering instead of loading every lock document into memory.

        if(!expiredLocks.isEmpty()){
            writeLockRepository.deleteAll(expiredLocks);
            expiredLocks.forEach(lock -> {
                log.info("Expired lock released - rulebookId: {}, heldBy: {}",
                        lock.getRulebookId(), lock.getHeldByUserId());
                eventPublisher.publishLockReleased(
                        lock.getRulebookId(),
                        lock.getHeldByUserId(),
                        "expired");
            });
        }
    }
}
