package com.boardwise.backend.shared.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.boardwise.backend.user_service.dtos.NotificationDTO;
import com.boardwise.backend.user_service.repository.EventAttendeeRepository;
import com.boardwise.backend.user_service.repository.NotificationRepository;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notifRepo;

    public NotificationService(
        SimpMessagingTemplate messagingTemplate,
        EventAttendeeRepository eaRepo
    ){
        this.messagingTemplate = messagingTemplate;
        this.eaRepo = eaRepo;
    }

    @Async
    public void notifyUser(String receiver, NotificationDTO notification){
        // TODO: Add notifications persistence

        messagingTemplate.convertAndSendToUser(
            receiver, 
            "/queue/notification", 
            notification
        );
    }

    @Async
    public void notifyCommunity(String communityId, String senderId, NotificationDTO notification){
        // TODO: Add notifications peristence

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

}
