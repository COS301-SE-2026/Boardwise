package com.boardwise.backend.user_service.services;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.user_service.dtos.DeRsvpDTO;
import com.boardwise.backend.user_service.dtos.EventInfoDTO;
import com.boardwise.backend.user_service.dtos.EventInviteDTO;
import com.boardwise.backend.user_service.dtos.EventUpdateDTO;
import com.boardwise.backend.user_service.models.Event;
import com.boardwise.backend.user_service.repos.EventsRepository;

@Service
public class CommunityService {

    private final EventsRepository eventRepo;

    CommunityService(EventsRepository eventRepo){
        this.eventRepo = eventRepo;
    }

    public Map<String, Object> createEvent(String token, EventInfoDTO eventInfo, MultipartFile eventImg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createEvent'");
    }

    public Map<String, Object> updateEvent(String token, EventUpdateDTO newInfo, MultipartFile newImage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEvent'");
    }

    public Map<String, Object> rsvp(String token, String eventId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rsvp'");
    }

    public Map<String, Object> deRsvp(String token, DeRsvpDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deRsvp'");
    }

    public Map<String, Object> inviteToEvent(String token, EventInviteDTO inviteInfo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inviteToEvent'");
    }

    public Map<String, Object> deleteEvent(String token, String eventId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteEvent'");
    }

    public Map<String, Object> getEvents() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEvents'");
    }
}
