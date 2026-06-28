package com.boardwise.backend.user_service.repos;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.user_service.models.EventAttendee;

public interface EventAttendeeRepository extends MongoRepository<EventAttendee, String> {

    Optional<EventAttendee> deleteByUserIdAndEventId(String userId, String eventId);
}
