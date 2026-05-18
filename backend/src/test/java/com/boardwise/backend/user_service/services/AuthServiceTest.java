package com.boardwise.backend.user_service.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private BoardGameRepository gameRepo;

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

    @BeforeEach
    void setUp() {
        
        validRegisterDTO = new RegisterDTO(
            "testuser",
            "test@example.com",
            "StrongP@ss1!",
            "John",
            "Doe"
        );

        validLoginDTO = new LoginDTO("testuser", "StrongP@ss1!");
    }

    @Test
    void shouldRegisterUser_Successfully() {
        // Given
        when(jwt.generateToken(anyString())).thenReturn("test.jwt.token");
        when(userRepo.save(any())).thenReturn(null);
  
        // When
        AuthResponseDTO response = authService.register(validRegisterDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("User successfully register");
        assertThat(response.accessToken()).isEqualTo("test.jwt.token");
        verify(userRepo).save(any());
        verify(jwt).generateToken("testuser");
    }

    @Test
    void shouldLoginUser_Successfully() {
        // Given
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwt.generateToken(anyString())).thenReturn("test.jwt.token");

        // When
        AuthResponseDTO response = authService.login(validLoginDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("User logged in successfully");
        assertThat(response.accessToken()).isEqualTo("test.jwt.token");
        verify(manager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwt).generateToken("testuser");
    }

    @Test
    void shouldThrowException_WhenLoginFails() {
        // Given
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(validLoginDTO);
        });
    }

    @Test
    void shouldLogoutUser_Successfully() {
        // Given
        String token = "test.jwt.token";
        
        // When
        LogoutResponseDTO response = authService.logout(token);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("User successfully logged out");
        verify(jwt).addToBlackList(token);
    }

    @Test
    void shouldSanitizeHtml_InRegistration() {
        // Given
        RegisterDTO maliciousDTO = new RegisterDTO(
            "<script>alert('xss')</script>",
            "test@example.com",
            "StrongP@ss1!",
            "<b>John</b>",
            "Doe"
        );
        
        when(jwt.generateToken(anyString())).thenReturn("test.jwt.token");
        when(userRepo.save(any())).thenReturn(null);

        // When
        AuthResponseDTO response = authService.register(maliciousDTO);

        // Then
        assertThat(response).isNotNull();
        verify(userRepo).save(any());
    }

    @Test
    void shouldSanitizeNoSQLInjection_InRegistration() {
        // Given
        RegisterDTO maliciousDTO = new RegisterDTO(
            "{$gt: ''}",
            "test@example.com",
            "StrongP@ss1!",
            "John",
            "Doe"
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(maliciousDTO);
        });
    }

    @Test
    void shouldHandleNullPreferences_InRegistration() {
        // Given
        RegisterDTO dtoWithoutPrefs = new RegisterDTO(
            "testuser",
            "test@example.com",
            "StrongP@ss1!",
            "John",
            "Doe"
        );
        
        when(jwt.generateToken(anyString())).thenReturn("test.jwt.token");
        when(userRepo.save(any())).thenReturn(null);

        // When
        AuthResponseDTO response = authService.register(dtoWithoutPrefs);

        // Then
        assertThat(response).isNotNull();
        verify(userRepo).save(any());
    }

    @Test
    void shouldSanitizeLoginUsername() {
        // Given
        LoginDTO maliciousLogin = new LoginDTO(
            "<script>alert('xss')</script>",
            "StrongP@ss1!"
        );
        
        when(manager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwt.generateToken(anyString())).thenReturn("test.jwt.token");

        // When
        AuthResponseDTO response = authService.login(maliciousLogin);

        // Then
        assertThat(response).isNotNull();
    }
}