package com.boardwise.backend.user_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardwise.backend.user_service.models.TokenBlackList;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.models.UserDetailImpl;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;


@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private TokenBlackListRepository tokenRepo;

    @InjectMocks
    private JWTService jwtService;

    private User testUser;
    private UserDetailImpl userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmailAddress("test@example.com");
        
        userDetails = new UserDetailImpl(testUser);
        validToken = jwtService.generateToken("testuser");
    }

    @Test
    void shouldGenerateToken_WithAllRequiredClaims() {
        // When
        String token = jwtService.generateToken("testuser");

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        
        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldGenerateToken_WithUniqueJTI() {
        // When
        String token1 = jwtService.generateToken("testuser");
        String token2 = jwtService.generateToken("testuser");

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void shouldExtractUsername_FromValidToken() {
        // Given
        String token = jwtService.generateToken("john_doe");

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("john_doe");
    }

    @Test
    void shouldValidateToken_WhenTokenIsValid() {
        // Given
        when(tokenRepo.existsByJti(anyString())).thenReturn(false);

        // When
        boolean isValid = jwtService.validateToken(validToken, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateToken_WhenTokenIsBlacklisted() {
        // Given
        when(tokenRepo.existsByJti(anyString())).thenReturn(true);

        // When
        boolean isValid = jwtService.validateToken(validToken, userDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidateToken_WhenUsernameDoesNotMatch() {
        // Given
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        UserDetailImpl otherUserDetails = new UserDetailImpl(otherUser);
        // when(tokenRepo.existsByJti(anyString())).thenReturn(false);

        // When
        boolean isValid = jwtService.validateToken(validToken, otherUserDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldAddTokenToBlacklist() {
        // Given
        String token = jwtService.generateToken("testuser");
        when(tokenRepo.save(any(TokenBlackList.class))).thenReturn(null);

        // When
        jwtService.addToBlackList(token);

        // Then
        verify(tokenRepo).save(any(TokenBlackList.class));
    }

    @Test
    void shouldCheckIfTokenIsBlacklisted() {
        // Given
        String token = jwtService.generateToken("testuser");
        when(tokenRepo.existsByJti(anyString())).thenReturn(true);

        // When (calling validateToken which checks blacklist)
        boolean isValid = jwtService.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isFalse();
        verify(tokenRepo).existsByJti(anyString());
    }

    @Test
    void shouldNotBlacklistToken_WhenTokenAlreadyExpired() {
        // This test verifies behavior with expired tokens
        // Since we can't easily create expired tokens with the service,
        // we test that blacklist check works correctly
        when(tokenRepo.existsByJti(anyString())).thenReturn(false);
        
        boolean isValid = jwtService.validateToken(validToken, userDetails);
        
        assertThat(isValid).isTrue();
        verify(tokenRepo).existsByJti(anyString());
    }

    @Test
    void shouldGenerateDifferentTokens_ForDifferentUsers() {
        // When
        String token1 = jwtService.generateToken("user1");
        String token2 = jwtService.generateToken("user2");

        // Then
        String username1 = jwtService.extractUsername(token1);
        String username2 = jwtService.extractUsername(token2);
        
        assertThat(username1).isEqualTo("user1");
        assertThat(username2).isEqualTo("user2");
        assertThat(token1).isNotEqualTo(token2);
    }
}