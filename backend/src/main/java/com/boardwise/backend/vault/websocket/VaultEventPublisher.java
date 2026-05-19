package com.boardwise.backend.vault.websocket;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.boardwise.backend.vault.dto.response.DeltaBroadcastDto;
import com.boardwise.backend.vault.dto.response.LockAcquiredEventDto;
import com.boardwise.backend.vault.dto.response.LockReleasedEventDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VaultEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    private static final String TOPIC_BASE = "/topic/vault/rulebooks/";

    public void publishLockAcquired(ObjectId rulebookId, ObjectId lockedBy,
            Instant expiresAt, int currentVersion) {
        LockAcquiredEventDto event = LockAcquiredEventDto.builder()
                .rulebookId(rulebookId.toHexString())
                .lockedBy(lockedBy.toHexString())
                .expiresAt(expiresAt)
                .currentVersion(currentVersion)
                .build();

        messagingTemplate.convertAndSend(
                TOPIC_BASE + rulebookId.toHexString() + "/lock/acquired",
                event);
    }
    
    public void publishLockReleased(ObjectId rulebookId, ObjectId releasedBy, String reason){
        LockReleasedEventDto event = LockReleasedEventDto.builder()
            .rulebookId(rulebookId.toHexString())
            .releasedBy(releasedBy.toHexString())
            .reason(reason)
            .releasedAt(Instant.now())
            .build();

        messagingTemplate.convertAndSend(
            TOPIC_BASE + rulebookId.toHexString() + "/lock/released",
            event
        );
   }

    public void publishDelta(ObjectId rulebookId,
        ObjectId editorId, String delta, int newVersion,
        Instant committedAt){
            DeltaBroadcastDto broadcast = DeltaBroadcastDto.builder()
                .rulebookId(rulebookId.toHexString())
                .editorId(editorId.toHexString())
                .delta(delta)
                .newVersion(newVersion)
                .committedAt(committedAt)
                .build();

        messagingTemplate.convertAndSend(
            TOPIC_BASE + rulebookId.toHexString() + "/delta",
            broadcast
        );
    }
}
