package com.boardwise.backend.user_service.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.models.Friendship;

public interface FriendShipRepository extends MongoRepository<Friendship, String> {
    
    @Query("{ '$or': [{ 'sender': ?0 }, { 'receiver': ?0 }], 'status': ?1 }")
    List<Friendship> findByUserAndStatus(String userId, FriendStatus status);

    @Query("{ '$or': [{ 'sender': ?0 , 'receiver': ?1 }, { 'sender': ?1 , 'receiver': ?0 }] }")
    Optional<Friendship> findFriendShipBetweenUsers(String userAId, String userBId);
}

