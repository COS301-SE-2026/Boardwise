package com.boardwise.backend.shared.services;

import java.util.List;



// import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.boardwise.backend.user_service.dtos.Notification;
import com.boardwise.backend.user_service.enums.RSVPStatus;
import com.boardwise.backend.user_service.models.EventAttendee;
import com.boardwise.backend.user_service.repos.EventAttendeeRepository;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final EventAttendeeRepository eaRepo;

    public NotificationService(
        SimpMessagingTemplate messagingTemplate,
        EventAttendeeRepository eaRepo
    ){
        this.messagingTemplate = messagingTemplate;
        this.eaRepo = eaRepo;
    }

    public void send(String receiver, Notification notification){
        messagingTemplate.convertAndSendToUser(
            receiver, 
            "/queue/notification", 
            notification
        );
    }

    @Async
    public void notifyAttendeesOfEventUpdates(String eventId, String changes){
        List<EventAttendee> attendees = eaRepo.findAllByEventIdAndStatus(eventId, RSVPStatus.ATTENDING);

        // TODO: Work out payload

        for(EventAttendee attendee : attendees){
            messagingTemplate.convertAndSendToUser(
                attendee.getUserId(),
                "/queue/notifications",
                "Payload here"
            );
        }

        messagingTemplate.convertAndSend(changes, attendees);
    }

    // @Async
    // public void notifyAttendeesOfEventCancellation(Map<String, String> eventInfo){
        
    // }
}
