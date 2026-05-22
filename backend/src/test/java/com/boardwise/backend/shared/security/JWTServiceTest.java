package com.boardwise.backend.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.models.TokenBlackList;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.models.UserDetailImpl;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    // The JWT secret used in application-test.properties
    private static final String TEST_SECRET = "this-is-a-dummy-secret-key-for-testing-purposes-only-do-not-use";

    @Mock
    private TokenBlackListRepository tokenRepo;

    private JWTService jwtService;

    private User testUser;
    private UserDetailImpl userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "key", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "tokenRepo", tokenRepo);

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmailAddress("test@example.com");

        userDetails = new UserDetailImpl(testUser);

        // Use a real string for userId — anyString() is a Mockito matcher
        // and returns null when called outside a when()/verify() context.
        validToken = jwtService.generateToken("testuser", "user-id-123");
    }

    // --- TOKEN GENERATION --------------------------------------------------------

    @Test
    void shouldGenerateToken_WithAllRequiredClaims() {
        String token = jwtService.generateToken("testuser", "user-id-123");

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature

        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldGenerateToken_WithUniqueJTI() {
        // Two tokens for the same user should be different (UUID-based JTI)
        String token1 = jwtService.generateToken("testuser", "user-id-123");
        String token2 = jwtService.generateToken("testuser", "user-id-123");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void shouldGenerateDifferentTokens_ForDifferentUsers() {
        String token1 = jwtService.generateToken("user1", "id-1");
        String token2 = jwtService.generateToken("user2", "id-2");

        assertThat(jwtService.extractUsername(token1)).isEqualTo("user1");
        assertThat(jwtService.extractUsername(token2)).isEqualTo("user2");
        assertThat(token1).isNotEqualTo(token2);
    }

    // --- USERNAME EXTRACTION -----------------------------------------------------

    @Test
    void shouldExtractUsername_FromValidToken() {
        String token = jwtService.generateToken("john_doe", "some-id");

        assertThat(jwtService.extractUsername(token)).isEqualTo("john_doe");
    }

    // --- TOKEN VALIDATION --------------------------------------------------------

    @Test
    void shouldValidateToken_WhenTokenIsValid() {
        when(tokenRepo.existsByJti(anyString())).thenReturn(false);

        boolean isValid = jwtService.validateToken(validToken, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldInvalidateToken_WhenTokenIsBlacklisted() {
        when(tokenRepo.existsByJti(anyString())).thenReturn(true);

        boolean isValid = jwtService.validateToken(validToken, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldInvalidateToken_WhenUsernameDoesNotMatch() {
        User otherUser = new User();
        otherUser.setUsername("otheruser");
        UserDetailImpl otherUserDetails = new UserDetailImpl(otherUser);

        boolean isValid = jwtService.validateToken(validToken, otherUserDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void shouldCheckIfTokenIsBlacklisted_DuringValidation() {
        String token = jwtService.generateToken("testuser", "user-id-123");
        when(tokenRepo.existsByJti(anyString())).thenReturn(true);

        boolean isValid = jwtService.validateToken(token, userDetails);

        assertThat(isValid).isFalse();
        verify(tokenRepo).existsByJti(anyString());
    }

    @Test
    void shouldReturnTrue_WhenTokenIsNotBlacklisted() {
        when(tokenRepo.existsByJti(anyString())).thenReturn(false);

        boolean isValid = jwtService.validateToken(validToken, userDetails);

        assertThat(isValid).isTrue();
        verify(tokenRepo).existsByJti(anyString());
    }

    // --- BLACKLIST ---------------------------------------------------------------

    @Test
    void shouldAddTokenToBlacklist() {
        String token = jwtService.generateToken("testuser", "user-id-123");
        when(tokenRepo.save(any(TokenBlackList.class))).thenReturn(null);

        jwtService.addToBlackList(token);

        verify(tokenRepo).save(any(TokenBlackList.class));
    }
}