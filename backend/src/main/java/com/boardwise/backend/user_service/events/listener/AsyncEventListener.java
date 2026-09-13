package com.boardwise.backend.user_service.events.listener;

import java.security.Principal;
import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.notifications.PresenceNotification;
import com.boardwise.backend.user_service.events.JoinedCommunityEvent;
import com.boardwise.backend.user_service.events.payload.JoinedCommunityEventPayload;
import com.boardwise.backend.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AsyncEventListener{

    private final UserRepository userRepo;
    private final SimpUserRegistry simpUserRegistry;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if(user == null) 
            return;

        String userId = user.getName();
        SimpUser simpUser = simpUserRegistry.getUser(userId);
        boolean sessionExist = simpUser != null && !simpUser.getSessions().isEmpty();
        System.out.println("[Async Event Listener]: User, \"" + userId + "\" has at least one existing session: " + sessionExist);

        if(!sessionExist){
            System.out.println("[Async Event Listener]: Broadcasting OFFLINE for user " + userId);
            broadcastUserPresence(userId, false, Instant.now());
            System.out.println("[Async Event Listener]: Broadcast call completed for user " + userId);
        }
    }

    @Async 
    @EventListener 
    public void handleConnect(SessionConnectedEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if(user == null) 
            return;

        String userId = user.getName();
        SimpUser simpUser = simpUserRegistry.getUser(userId);
        int sessions = simpUser != null ? simpUser.getSessions().size() : 0;
        System.out.println("[Async Event Listener]: Sessions for user, \"" + userId + "\": " + sessions);

        if(sessions == 1){
            broadcastUserPresence(userId, true, null);
        }
    }

    @Async 
    @EventListener 
    public void handledJoinedCommunityEvent(JoinedCommunityEvent event){
        JoinedCommunityEventPayload payload = event.getMessage();
        notificationService.notifyCommunity(payload.communityId(), payload.notification());
    }

    private void broadcastUserPresence(String userId, boolean isOnline, Instant lastOnlineAt){
        userRepo.updateLastOnlineAtByUserId(userId, lastOnlineAt);
        PresenceNotification notification = new PresenceNotification(userId, isOnline);
        notificationService.broadcastPresence(userId, notification);
    }
}
