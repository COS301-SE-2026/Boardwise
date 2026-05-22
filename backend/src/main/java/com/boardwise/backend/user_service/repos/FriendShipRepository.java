package com.boardwise.backend.user_service.repos;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.Friendship;

public interface FriendShipRepository extends MongoRepository<Friendship, String> {
    long countByUserAIdOrUserBId(String userAId, String userBId);
}
