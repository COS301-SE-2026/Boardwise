package com.boardwise.backend.user_service.repos;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.user_service.models.EventAttendee;
import com.boardwise.backend.user_service.models.RSVPStatus;

public interface EventAttendeeRepository extends MongoRepository<EventAttendee, String> {

    Optional<EventAttendee> findByUserIdAndEventId(String userId, String eventId);
    
    List<EventAttendee> findAllByEventIdAndStatus(String eventId, RSVPStatus status);

    List<EventAttendee> findAllByUserIdAndStatus(String userId, RSVPStatus status);

    void deleteByEventId(String eventId);
}
