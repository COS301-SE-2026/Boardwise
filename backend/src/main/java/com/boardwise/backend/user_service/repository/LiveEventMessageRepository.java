package com.boardwise.backend.user_service.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.LiveEventMessage;

public interface LiveEventMessageRepository extends MongoRepository<LiveEventMessage, String> {
    List<LiveEventMessage> findByEventIdOrderByCreatedAtAsc(String eventId);
    List<LiveEventMessage> findByEventIdAndCreatedAtAfterOrderByCreatedAtAsc(String eventId, Instant after);
    void deleteByEventId(String eventId);
}
