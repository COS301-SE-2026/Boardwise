package com.boardwise.backend.vault.messaging;

import java.security.Principal;

import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.boardwise.backend.vault.dto.response.DeltaCommitedEventDto;
import com.boardwise.backend.vault.dto.response.LockReleasedEventDto;
import com.boardwise.backend.vault.service.WriteLockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class VaultWebSocketDispatcher {
    private final SimpMessagingTemplate messagingTemplate;
    private final WriteLockService writeLockService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeltaCommitedEvent(DeltaCommitedEventDto event){
        String destination = "/topic/vault/rulebooks/" + event.rulebookId() + "/delta";

        messagingTemplate.convertAndSend(destination, event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLockReleased(LockReleasedEventDto event) {
        String destination = "/topic/vault/rulebooks/" + event.rulebookId() + "/lock/released";

        messagingTemplate.convertAndSend(destination, event);
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event){
        Principal user = event.getUser();

        if(user != null){
            String userId = user.getName();
            log.info("User {} disconnected from WebSocket. Reason: {}", userId, event.getCloseStatus());

            try{
                writeLockService.releaseAllWriteLocksForUser(new ObjectId(userId));
            }catch(Exception e){
                log.error("Failed to release lock for disconnected user {}", userId, e);
            }
        }
    }
}
