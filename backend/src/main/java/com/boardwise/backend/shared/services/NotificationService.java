package com.boardwise.backend.shared.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.boardwise.backend.user_service.dtos.notifications.ChatNotification;
import com.boardwise.backend.user_service.dtos.notifications.FriendConfirmationNotification;
import com.boardwise.backend.user_service.dtos.notifications.FriendRequestNotification;
import com.boardwise.backend.user_service.dtos.notifications.InviteNotification;
import com.boardwise.backend.user_service.dtos.notifications.NotificationDTO;
import com.boardwise.backend.user_service.dtos.notifications.PresenceNotification;
import com.boardwise.backend.user_service.enums.NotificationType;
import com.boardwise.backend.user_service.models.ChatMessageData;
import com.boardwise.backend.user_service.models.EventInviteData;
import com.boardwise.backend.user_service.models.FriendConfirmationData;
import com.boardwise.backend.user_service.models.FriendRequestData;
import com.boardwise.backend.user_service.models.NotificationData;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    @Async
    public void notifyUser(String receiver, NotificationDTO notification){
        messagingTemplate.convertAndSendToUser(
            receiver, 
            "/queue/notification", 
            notification
        );
    }

    @Async 
    public void broadcastPresence(String userId, NotificationDTO notification){
        System.out.println("[Presence] broadcastPresence called, type=" + notification.getType());
        if(notification.getType() != NotificationType.PRESENCE){
            System.out.println("[Presence]: Type mismatch, aborting broadcast");
            return;
        } 

        messagingTemplate.convertAndSend(
            "/topic/presence/" + userId,
            notification
        );
    }

    @Async
    public void notifyCommunity(String communityId, NotificationDTO notification){
        if(notification.getType() != NotificationType.COMMUNITY_CHAT) return;

        messagingTemplate.convertAndSend(
            "/topic/community/" + communityId + "/notification",
            notification 
        );
    }

    @Async
    public void notifyEventAttendees(String eventId, NotificationDTO notification){
        // TODO: Add notifications peristence

        messagingTemplate.convertAndSend(
            "/topic/event/" + eventId + "/notification",
            notification 
        );
    }

    public Boolean isOnline(String userId){
        SimpUser user = userRegistry.getUser(userId);
        return user != null && !user.getSessions().isEmpty();
    }

    private NotificationData makeNotificationData(NotificationDTO notification){
        return switch(notification){
            case ChatNotification dto -> new ChatMessageData(dto.senderId(), dto.message());
            case FriendConfirmationNotification dto -> new FriendConfirmationData(dto.friend());
            case FriendRequestNotification dto -> new FriendRequestData(dto.request());
            case InviteNotification dto -> new EventInviteData(dto.host(), dto.event());
            case PresenceNotification dto -> null;
        };
    }
}
