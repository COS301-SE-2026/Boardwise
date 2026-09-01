package com.boardwise.backend.user_service.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.EmailService;
import com.boardwise.backend.user_service.dtos.request.ForgotPasswordDto;
import com.boardwise.backend.user_service.dtos.request.ResetPasswordDto;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.user_service.utils.PasswordResetTokenUtils;

@DisplayName("Authentication Service Unit Tests")
@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepo;
    
    @Mock
    private JWTService jwt;

    @Mock
    private AuthenticationManager manager;

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<User> userCaptor;


    @Nested
    class ForgotPasswordTests{
        @Test
        void shouldProcessValidEmail(){
            // Arrange
            String email = "test@example.com";
            User mockUser = new User();
            mockUser.setEmailAddress(email);

            when(userRepo.findByEmailAddress(email)).thenReturn(Optional.of(mockUser));
            
            // Act
            authService.forgotPassword(new ForgotPasswordDto(email));
            
            // Assert
            verify(userRepo).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertNotNull(savedUser.getResetToken());
            assertNotNull(savedUser.getResetTokenExpiry());
            assertTrue(savedUser.getResetTokenExpiry().isAfter(Instant.now()));

            verify(emailService).sendPasswordResetEmail(eq(email), anyString());
        }

        @Test
        void shouldIgnoreUnregisteredEmail(){
            // Arrange
            String email = "test@example.com";
            when(userRepo.findByEmailAddress(email)).thenReturn(Optional.empty());
            
            // Act
            authService.forgotPassword(new ForgotPasswordDto(email));

            // Assert
            verify(userRepo, never()).save(any(User.class));
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
        }
    }

    @Nested
    class ResetPasswordTests{
        @Test
        void shouldResetPasswordSuccessfully(){
            // Arrange
            String mockToken = "mock-token-1";
            String newPassword = "new-password";
            String hashedToken = PasswordResetTokenUtils.hashToken(mockToken);

            User mockUser = new User();
            mockUser.setResetTokenExpiry(Instant.now().plusSeconds(600));

            when(userRepo.findByResetToken(hashedToken)).thenReturn(Optional.of(mockUser));
            
            // Act
            authService.resetPassword(new ResetPasswordDto(mockToken, newPassword));
            
            // Assert
            verify(userRepo).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertNotEquals(newPassword, savedUser.getPassword(), "Password should be hashed, not plain text");
            assertTrue(savedUser.getPassword().startsWith("$2a$"),"Password should be a valid BCrypt hash");
            assertNull(savedUser.getResetToken());
            assertNull(savedUser.getResetTokenExpiry());
        }

        @Test
        void shouldThrowExceptionForInvalidToken(){
            // Arrange
            String token = "invalid-token";
            String hashedToken = PasswordResetTokenUtils.hashToken(token);

            when(userRepo.findByResetToken(hashedToken)).thenReturn(Optional.empty());
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.resetPassword(new ResetPasswordDto(token, "password"))
            );
            assertEquals("Invalid password reset token.", exception.getMessage());
            verify(userRepo, never()).save(any(User.class));
        }

        @Test
        void shouldThrowExceptionForExpiredToken(){
            // Arrange
            String token = "expired-token";
            String hashedToken = PasswordResetTokenUtils.hashToken(token);

            User mockUser = new User();
            mockUser.setResetTokenExpiry(Instant.now().minusSeconds(3600));

            when(userRepo.findByResetToken(hashedToken)).thenReturn(Optional.of(mockUser));
            
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.resetPassword(new ResetPasswordDto(token, "password"))
            );
            assertEquals("Password reset token expired", exception.getMessage());
            verify(userRepo, never()).save(any(User.class));
        }
    }
}
