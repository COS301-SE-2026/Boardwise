package com.boardwise.backend.user_service.services;

import java.security.Principal;
import java.time.Instant;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.boardwise.backend.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OfflineEventListener {

    private final UserRepository userRepo;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();
        if(user != null){
            userRepo.updateLastOnlineAtByUserId(user.getName(), Instant.now());
        }
    }

}
