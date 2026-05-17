package com.boardwise.backend.user_service;


import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.boardwise.backend.user_service.models.TokenBlackList;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;

@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseTest {

    @Autowired
    protected TokenBlackListRepository tokenBlackListRepository;

    @BeforeEach
    void cleanDatabase() {
        if (tokenBlackListRepository != null) {
            tokenBlackListRepository.deleteAll();
        }
    }

    protected TokenBlackList createTestToken(String jti) {
        return new TokenBlackList(
            jti,
            Instant.now().plusSeconds(1 * 60 * 60)
        );
    }

    protected String generateTestJti() {
        return UUID.randomUUID().toString();
    }
}
