package com.boardwise.backend.user_service.events.listener;

import java.security.Principal;
import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
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

        if(!sessionExist){
            broadcastUserPresence(userId, true, Instant.now());
        }
    }

    @Async 
    @EventListener 
    public void handleConnect(SessionConnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if(user == null) 
            return;

        String userId = user.getName();
        SimpUser simpUser = simpUserRegistry.getUser(userId);
        int sessions = simpUser != null ? simpUser.getSessions().size() : 0;

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
