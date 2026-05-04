package com.boardwise.backend.user_service.repos;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.boardwise.backend.user_service.models.TokenBlackList;

public interface TokenBlackListRepository extends MongoRepository<TokenBlackList, String> {
    boolean existsByJti(String jti);

    @Query("{'expiresAt': {$lt: ?0}}")
    void deleteAllExpired(LocalDateTime date);
}
