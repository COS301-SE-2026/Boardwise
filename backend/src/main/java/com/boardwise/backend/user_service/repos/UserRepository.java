package com.boardwise.backend.user_service.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.User;

public interface UserRepository extends MongoRepository<User, String>{

    public Optional<User> findByUsername(String username);
    
    @DeleteQuery("{'username': ?0}")
    public long deleteByUsername(String username);

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


}
