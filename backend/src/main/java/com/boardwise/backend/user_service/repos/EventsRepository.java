package com.boardwise.backend.user_service.repos;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.user_service.models.Event;

public interface EventsRepository extends MongoRepository<Event, String>{
    List<Event> findAllBy(TextCriteria criteria, Pageable pageable);
}
