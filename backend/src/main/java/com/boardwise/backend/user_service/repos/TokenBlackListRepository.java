package com.boardwise.backend.user_service.repos;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.TokenBlackList;

public interface TokenBlackListRepository extends MongoRepository<TokenBlackList, String> {
    boolean existsByJti(String jti);

    @DeleteQuery("{'expiresAt': {$lt: ?0}}")
    void deleteAllExpired(LocalDateTime date);
}
