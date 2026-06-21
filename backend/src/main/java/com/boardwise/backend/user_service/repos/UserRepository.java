package com.boardwise.backend.user_service.repos;

import java.util.Optional;

import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.User;

public interface UserRepository extends MongoRepository<User, String>{

    public Optional<User> findByUsername(String username);
    
    @DeleteQuery("{'username': ?0}")
    public long deleteByUsername(String username);
}
