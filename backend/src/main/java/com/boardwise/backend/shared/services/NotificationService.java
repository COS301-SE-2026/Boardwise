package com.boardwise.backend.shared.services;

import java.time.Instant;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.boardwise.backend.user_service.dtos.ChatNotification;
import com.boardwise.backend.user_service.dtos.FriendConfirmationNotification;
import com.boardwise.backend.user_service.dtos.FriendRequestNotification;
import com.boardwise.backend.user_service.dtos.InviteNotification;
import com.boardwise.backend.user_service.dtos.NotificationDTO;
import com.boardwise.backend.user_service.enums.NotificationType;
import com.boardwise.backend.user_service.models.ChatMessageData;
import com.boardwise.backend.user_service.models.EventInviteData;
import com.boardwise.backend.user_service.models.FriendConfirmationData;
import com.boardwise.backend.user_service.models.FriendRequestData;
import com.boardwise.backend.user_service.models.GroupMembership;
import com.boardwise.backend.user_service.models.Notification;
import com.boardwise.backend.user_service.models.NotificationData;
import com.boardwise.backend.user_service.repository.GroupMembershipRepository;
import com.boardwise.backend.user_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notifRepo;
    private final SimpUserRegistry userRegistry;
    private final GroupMembershipRepository groupRepo;

    @Async
    public void notifyUser(String receiver, NotificationDTO notification){
        messagingTemplate.convertAndSendToUser(
            receiver, 
            "/queue/notification", 
            notification
        );

        if(!isOnline(receiver)){
            Notification notif = new Notification(
                null,
                receiver,
                notification.getType(),
                makeNotificationData(notification),
                Instant.now(),
                false,
                null
            );
            // notifRepo.save(notif);
        }
    }

    @Async
    public void notifyCommunity(String communityId, NotificationDTO notification){
        if(notification.getType() != NotificationType.COMMUNITY_CHAT) return;

        List<GroupMembership> memberships = groupRepo.findByGroupId(communityId);
        List<Notification> notifications = memberships.stream()
                                                    .filter((membership) -> !isOnline(membership.getUserId()))
                                                    .map((membership) -> new Notification(
                                                        null,
                                                        membership.getUserId(),
                                                        notification.getType(),
                                                        makeNotificationData(notification),
                                                        Instant.now(),
                                                        false,
                                                        null
                                                    )).toList();
        
        // if(!notifications.isEmpty()) notifRepo.saveAll(notifications);

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
        return userRegistry.getUser(userId) != null;
    }

    private NotificationData makeNotificationData(NotificationDTO notification){
        return switch(notification){
            case ChatNotification dto -> new ChatMessageData(dto.senderId(), dto.message());
            case FriendConfirmationNotification dto -> new FriendConfirmationData(dto.friend());
            case FriendRequestNotification dto -> new FriendRequestData(dto.request());
            case InviteNotification dto -> new EventInviteData(dto.host(), dto.event());
        };
    }
}
