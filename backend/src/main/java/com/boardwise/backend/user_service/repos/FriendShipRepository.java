package com.boardwise.backend.user_service.repos;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.models.Friendship;

public interface FriendShipRepository extends MongoRepository<Friendship, String> {
    long countBySenderOrReceiver(String sender, String receiver);

    @Query("{ '$or': [{ 'sender': ?0 }, { 'receiver': ?0 }], 'status': ?1 }")
    List<Friendship> findByUserAndStatus(String userId, FriendStatus status);
}

