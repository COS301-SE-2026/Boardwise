package com.boardwise.backend.vault.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.boardwise.backend.vault.dto.response.DeltaCommitedEventDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VaultWebSocketDispatcher {
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeltaCommitedEvent(DeltaCommitedEventDto event){
        String destination = "/topic/vault/rulebooks/" + event.rulebookId().toHexString();

        messagingTemplate.convertAndSend(destination, event);
    }
}
