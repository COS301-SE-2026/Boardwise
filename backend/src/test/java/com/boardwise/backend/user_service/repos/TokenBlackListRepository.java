package com.boardwise.backend.user_service.repos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.boardwise.backend.user_service.models.TokenBlackList;

@DataMongoTest
@ActiveProfiles("test")
class TokenBlackListRepositoryTest {

    @Autowired
    private TokenBlackListRepository tokenRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(TokenBlackList.class);
    }

    @Test
    void shouldSaveAndRetrieveToken() {
        // Given
        String jti = "test-jti-123";
        Instant expiresAt = Instant.now().plusSeconds(1 * 60 * 60);
        TokenBlackList token = new TokenBlackList(jti, expiresAt);

        // When
        TokenBlackList saved = tokenRepository.save(token);
        boolean exists = tokenRepository.existsByJti(jti);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_WhenTokenNotExists() {
        // When
        boolean exists = tokenRepository.existsByJti("non-existent-jti");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindToken_ByJti() {
        // Given
        String jti = "test-jti-456";
        TokenBlackList token = new TokenBlackList(jti, Instant.now().plusSeconds(1 * 60 * 60));
        tokenRepository.save(token);

        // When
        boolean exists = tokenRepository.existsByJti(jti);

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldDeleteExpiredTokens() {
        // Given
        TokenBlackList expiredToken = new TokenBlackList(
            "expired-jti",
            Instant.now().minusSeconds(1 * 60 * 60)
        );
        TokenBlackList validToken = new TokenBlackList(
            "valid-jti",
            Instant.now().plusSeconds(1 * 60 * 60)
        );
        
        tokenRepository.save(expiredToken);
        tokenRepository.save(validToken);

        // When
        tokenRepository.deleteAllExpired(Instant.now());

        // Then
        List<TokenBlackList> remainingTokens = tokenRepository.findAll();
        assertThat(remainingTokens).hasSize(1);
        assertThat(remainingTokens.get(0).getJti()).isEqualTo("valid-jti");
    }

    @Test
    void shouldStoreMultipleTokens() {
        // Given
        TokenBlackList token1 = new TokenBlackList("jti-1", Instant.now().plusSeconds(1 * 60 * 60));
        TokenBlackList token2 = new TokenBlackList("jti-2", Instant.now().plusSeconds(2 * 60 * 60));

        // When
        tokenRepository.save(token1);
        tokenRepository.save(token2);

        // Then
        List<TokenBlackList> allTokens = tokenRepository.findAll();
        assertThat(allTokens).hasSize(2);
    }

    @Test
    void shouldSetCreatedAt_Automatically() {
        // Given
        TokenBlackList token = new TokenBlackList("jti-test", Instant.now().plusSeconds(1 * 60 * 60));

        // When
        TokenBlackList saved = tokenRepository.save(token);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isBefore(Instant.now().plusSeconds(1));
    }
}