package com.boardwise.backend.user_service.repos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.boardwise.backend.user_service.models.TokenBlackList;

@DataMongoTest
@Testcontainers
class TokenBlackListRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    // These were missing — @DataMongoTest auto-wires them from the slice context
    @Autowired
    private TokenBlackListRepository tokenRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(TokenBlackList.class);
    }

    // --- SAVE & EXISTS -----------------------------------------------------------

    @Test
    void shouldSaveAndRetrieveToken() {
        String jti = "test-jti-123";
        Instant expiresAt = Instant.now().plusSeconds(3600);
        TokenBlackList token = new TokenBlackList(jti, expiresAt);

        TokenBlackList saved = tokenRepository.save(token);
        boolean exists = tokenRepository.existsByJti(jti);

        assertThat(saved.getId()).isNotNull();
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_WhenTokenNotExists() {
        boolean exists = tokenRepository.existsByJti("non-existent-jti");

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindToken_ByJti() {
        String jti = "test-jti-456";
        tokenRepository.save(new TokenBlackList(jti, Instant.now().plusSeconds(3600)));

        assertThat(tokenRepository.existsByJti(jti)).isTrue();
    }

    // --- DELETE EXPIRED ----------------------------------------------------------

    @Test
    void shouldDeleteExpiredTokens() {
        // Note: @DeleteQuery uses the Java field name from the query string.
        // The stored MongoDB field is "expires_at" (via @Field), but @DeleteQuery
        // operates on the Java field name "expiresAt" when using Spring Data
        // query derivation. If this test fails, it indicates the @DeleteQuery
        // value in the repository needs to reference the actual stored field name
        // "expires_at" rather than "expiresAt".
        tokenRepository.save(new TokenBlackList("expired-jti", Instant.now().minusSeconds(3600)));
        tokenRepository.save(new TokenBlackList("valid-jti", Instant.now().plusSeconds(3600)));

        tokenRepository.deleteAllExpired(Instant.now());

        List<TokenBlackList> remaining = tokenRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getJti()).isEqualTo("valid-jti");
    }

    // --- MULTIPLE TOKENS ---------------------------------------------------------

    @Test
    void shouldStoreMultipleTokens() {
        tokenRepository.save(new TokenBlackList("jti-1", Instant.now().plusSeconds(3600)));
        tokenRepository.save(new TokenBlackList("jti-2", Instant.now().plusSeconds(7200)));

        assertThat(tokenRepository.findAll()).hasSize(2);
    }

    // --- CREATED_AT --------------------------------------------------------------

    @Test
    void shouldSetCreatedAt_Automatically() {
        TokenBlackList token = new TokenBlackList("jti-test", Instant.now().plusSeconds(3600));

        TokenBlackList saved = tokenRepository.save(token);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBefore(Instant.now().plusSeconds(1));
    }
}