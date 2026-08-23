package com.boardwise.backend.user_service.repository;

import java.time.Instant;


import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.TokenBlackList;

public interface TokenBlackListRepository extends MongoRepository<TokenBlackList, String> {
    boolean existsByJti(String jti);

    @DeleteQuery("{'expiresAt': {$lt: ?0}}")
    void deleteAllExpired(Instant date);
}
