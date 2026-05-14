package com.boardwise.backend.vault.websocket;

import org.bson.types.ObjectId;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.boardwise.backend.vault.dto.response.RulebookTextResponseDto;
import com.boardwise.backend.vault.service.RulebookService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VaultWebSocketController {
    private final RulebookService rulebookService;
    private final SimpMessagingTemplate messagingTemplate;

    // client sends to /app/vault/rulebooks/{id}/sync
    // receives latest text state on /topic/vault/rulebooks/{id}/sync
    @MessageMapping("/vault/rulebooks/{id}/sync")
    public void syncRulebookText(@DestinationVariable String id){
        RulebookTextResponseDto text = rulebookService.getRulebookText(new ObjectId(id));

        messagingTemplate.convertAndSend(
            "/topic/vault/rulebooks/" + id + "/sync",
            text
        );
    }
}
