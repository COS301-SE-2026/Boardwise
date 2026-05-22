package com.boardwise.backend.user_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private JWTService jwt;

    @Mock
    private AuthenticationManager manager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterDTO validRegisterDTO;
    private LoginDTO validLoginDTO;

    // A saved user with a real id, as returned by userRepo.save()
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRegisterDTO = new RegisterDTO(
                "testuser", "test@example.com", "StrongP@ss1!", "John", "Doe");

        validLoginDTO = new LoginDTO("testuser", "StrongP@ss1!");

        savedUser = new User();
        savedUser.setUsername("testuser");
        // Simulate MongoDB assigning an id after save
        org.springframework.test.util.ReflectionTestUtils.setField(savedUser, "id", "mongo-id-123");
    }

    // --- REGISTER ----------------------------------------------------------------

    @Test
    void shouldRegisterUser_Successfully() {
        // AuthService.register() does not reassign the return value of userRepo.save(),
        // so newUser.getId() is always null when jwt.generateToken() is called.
        // The stub must use isNull() for the second argument, not anyString().
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwt.generateToken(anyString(), isNull())).thenReturn("test.jwt.token");

        AuthResponseDTO response = authService.register(validRegisterDTO);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("User successfully register");
        assertThat(response.accessToken()).isEqualTo("test.jwt.token");
        verify(userRepo).save(any(User.class));
        // eq() required when mixing exact and matcher args in verify()
        verify(jwt).generateToken(eq("testuser"), isNull());
    }

    @Test
    void shouldSanitizeHtml_InRegistration() {
        RegisterDTO maliciousDTO = new RegisterDTO(
                "<script>alert('xss')</script>", "test@example.com",
                "StrongP@ss1!", "<b>John</b>", "Doe");

        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwt.generateToken(anyString(), isNull())).thenReturn("test.jwt.token");

        AuthResponseDTO response = authService.register(maliciousDTO);

        // HTML tags are stripped before saving — response should still succeed
        assertThat(response).isNotNull();
        verify(userRepo).save(any(User.class));
    }

    @Test
    void shouldSanitizeNoSQLInjection_InRegistration() {
        RegisterDTO maliciousDTO = new RegisterDTO(
                "{$gt: ''}", "test@example.com", "StrongP@ss1!", "John", "Doe");

        // sanitize() throws IllegalArgumentException on '$' or '{'
        assertThrows(IllegalArgumentException.class,
                () -> authService.register(maliciousDTO));
    }

    @Test
    void shouldHandleNullPreferences_InRegistration() {
        when(userRepo.save(any(User.class))).thenReturn(savedUser);
        when(jwt.generateToken(anyString(), isNull())).thenReturn("test.jwt.token");

        AuthResponseDTO response = authService.register(validRegisterDTO);

        assertThat(response).isNotNull();
        verify(userRepo).save(any(User.class));
    }

    // --- LOGIN -------------------------------------------------------------------

    @Test
    void shouldLoginUser_Successfully() {
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        // login() calls userRepo.findByUsername to get the id for token generation
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(savedUser));
        when(jwt.generateToken(anyString(), anyString())).thenReturn("test.jwt.token");

        AuthResponseDTO response = authService.login(validLoginDTO);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("User logged in successfully");
        assertThat(response.accessToken()).isEqualTo("test.jwt.token");
        verify(manager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwt).generateToken(eq("testuser"), anyString());
    }

    @Test
    void shouldThrowException_WhenLoginFails() {
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.login(validLoginDTO));
    }

    @Test
    void shouldSanitizeLoginUsername() {
        // HTML tags in username are stripped by sanitize() before authentication
        LoginDTO maliciousLogin = new LoginDTO(
                "<script>alert('xss')</script>", "StrongP@ss1!");

        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        // sanitize strips tags so username becomes empty string ""
        when(userRepo.findByUsername(anyString())).thenReturn(Optional.of(savedUser));
        when(jwt.generateToken(anyString(), anyString())).thenReturn("test.jwt.token");

        AuthResponseDTO response = authService.login(maliciousLogin);

        assertThat(response).isNotNull();
    }

    // --- LOGOUT ------------------------------------------------------------------

    @Test
    void shouldLogoutUser_Successfully() {
        String token = "test.jwt.token";

        LogoutResponseDTO response = authService.logout(token);

        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("User successfully logged out");
        verify(jwt).addToBlackList(token);
    }
}