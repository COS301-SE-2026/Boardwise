package com.boardwise.backend.user_service.services;

import java.security.Principal;
import org.springframework.stereotype.Service;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.CommunityChatNotification;
import com.boardwise.backend.user_service.dtos.CommunityMessage;
import com.boardwise.backend.user_service.dtos.CommunityMessageDTO;
import com.boardwise.backend.user_service.dtos.DirectMessage;
import com.boardwise.backend.user_service.dtos.DirectMessageDTO;
import com.boardwise.backend.user_service.dtos.DirectMessageNotification;
import com.boardwise.backend.user_service.dtos.NotificationDTO;
import com.boardwise.backend.user_service.repository.MessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final NotificationService notifService;
    private final MessageRepository messageRepo;
    
    
    public DirectMessageDTO handleDirectMessage(Principal principal, DirectMessage message){
        String senderId = principal.getName();
        NotificationDTO notification;

        // send the notification
        notification = new DirectMessageNotification(
            senderId,
            message.message()
        );
        notifService.notifyUser(message.receiverId(), notification);
        

        // send the chat message for the ui
        // save the message first
        return new DirectMessageDTO(
            senderId,
            message
        );
    }

    public CommunityMessageDTO handleCommunityMessage(Principal principal, CommunityMessage message){
        String senderId = principal.getName();
        NotificationDTO notification;

        // send the notification
        notification = new CommunityChatNotification(
            senderId, 
            message.message()
        );
        notifService.notifyCommunity(message.communityId(), senderId, notification);


        // send the chat message for the ui (senderId will be used)
        // save the message first 

        return new CommunityMessageDTO(
            senderId, 
            message
        );
    }
}
