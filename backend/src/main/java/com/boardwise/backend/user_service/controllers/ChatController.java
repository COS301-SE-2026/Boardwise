package com.boardwise.backend.user_service.controllers;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.CommunityChatNotification;
import com.boardwise.backend.user_service.dtos.CommunityMessage;
import com.boardwise.backend.user_service.dtos.DirectMessage;
import com.boardwise.backend.user_service.dtos.DirectMessageDTO;
import com.boardwise.backend.user_service.dtos.DirectMessageNotification;
import com.boardwise.backend.user_service.dtos.Notification;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {
    // Will be used chat/chatrooms and other things that need it

    private final NotificationService notifService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.direct")
    public void processDirectMessage(
        @Payload DirectMessage message,
        Principal principal
    ){

        String senderId = principal.getName();
        Notification notification;

        // send the notification
        notification = new DirectMessageNotification(
            senderId,
            message.message()
        );
        notifService.send(message.receiverId(), notification);
        

        // send the chat message for the ui
        DirectMessageDTO chatMessage = new DirectMessageDTO(
            senderId,
            message
        );
        
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
        String senderId = principal.getName();
        Notification notification;

        // send the notification
        notification = new CommunityChatNotification(
            senderId, 
            message.message()
        );
        notifService.send(senderId, notification);


        // send the chat message for the ui (senderId will be used)
        var chatMessage = message.message();
        messagingTemplate.convertAndSend(
            "/topic/community/" + message.communityId() + "/chat",
            chatMessage
        );
    }
}
