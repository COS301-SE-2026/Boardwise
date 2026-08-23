package com.boardwise.backend.user_service.controllers;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.boardwise.backend.user_service.dtos.CommunityMessage;
import com.boardwise.backend.user_service.dtos.CommunityMessageDTO;
import com.boardwise.backend.user_service.dtos.DirectMessage;
import com.boardwise.backend.user_service.dtos.DirectMessageDTO;
import com.boardwise.backend.user_service.services.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.direct")
    public void processDirectMessage(
        @Payload DirectMessage message,
        Principal principal
    ){
        DirectMessageDTO chatMessage = service.handleDirectMessage(principal, message);
        messagingTemplate.convertAndSendToUser(
            message.receiverId(),
            "user/queue/chat",
            chatMessage
        );
    }

    @MessageMapping("/chat.community")
    public void processCommunityMessage(
        @Payload CommunityMessage message,
        Principal principal
    ){  
        CommunityMessageDTO chatMessage = service.handleCommunityMessage(principal, message);
        messagingTemplate.convertAndSend(
            "/topic/community/" + message.communityId() + "/chat",
            chatMessage
        );
    }
}
