package com.boardwise.backend.user_service.repos;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.Event;

public interface EventsRepository extends MongoRepository<Event, String>{

}
