package com.boardwise.backend.user_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.boardwise.backend.user_service.models.Conversation;


public interface ConversationRepository extends MongoRepository<Conversation, String> {
    @Query("{ 'participantIds': ?0 }")
    List<Conversation> participantIdsContainsUserId(String userId);
}
