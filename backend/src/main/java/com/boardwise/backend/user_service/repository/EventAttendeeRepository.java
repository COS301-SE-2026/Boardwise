package com.boardwise.backend.user_service.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.enums.RSVPStatus;
import com.boardwise.backend.user_service.models.EventAttendee;

public interface EventAttendeeRepository extends MongoRepository<EventAttendee, String> {

    Optional<EventAttendee> findByUserIdAndEventId(String userId, String eventId);
    
    List<EventAttendee> findAllByEventIdAndStatus(String eventId, RSVPStatus status);

    List<EventAttendee> findAllByUserIdAndStatus(String userId, RSVPStatus status);

    void deleteByEventId(String eventId);
}
