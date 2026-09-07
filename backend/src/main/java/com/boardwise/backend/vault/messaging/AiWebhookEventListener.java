package com.boardwise.backend.vault.messaging;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.boardwise.backend.vault.dto.websocket.ChunkDeletedEventDto;
import com.boardwise.backend.vault.dto.websocket.ChunkInsertedEventDto;
import com.boardwise.backend.vault.dto.websocket.DeltaCommitedEventDto;
import com.boardwise.backend.vault.service.AiGatewayWebhookService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiWebhookEventListener {
    private final AiGatewayWebhookService webhookService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeltaCommitted(DeltaCommitedEventDto event){
        webhookService.triggerReEmbedding(event.getChunkId(), event.getDeltaContent(), event.getMetadata());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChunkInserted(ChunkInsertedEventDto event){
        webhookService.triggerReEmbedding(event.getChunkId(), event.getContent(), event.getMetadata());
    }
}
