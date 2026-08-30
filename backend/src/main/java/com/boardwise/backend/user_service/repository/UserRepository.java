package com.boardwise.backend.user_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import com.boardwise.backend.user_service.models.User;

public interface UserRepository extends MongoRepository<User, String>{

    public Optional<User> findByUsername(String username);
    
    @DeleteQuery("{'username': ?0}")
    public long deleteByUsername(String username);

    @Query("{ 'id': ?0 }")
    @Update("{ $set: { 'lastOnlineAt': ?1 } }")
    public void updateLastOnlineAtByUserId(String userId, Instant lastOnline);

    //Recc system
    public interface GameOwnershipCount {
        String getId();
        int getCount();
    }
    
    @Aggregation(pipeline = {
        "{ $unwind: '$ownedGames' }",
        "{ $group: { _id: '$ownedGames', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }",
        "{ $limit: ?0 }"
    })
    List<GameOwnershipCount> findMostOwnedGameIds(int limit);

    public Optional<User> findByEmailAddress(String emailAddress);
}
