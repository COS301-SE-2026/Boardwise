package com.boardwise.backend.user_service.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.boardwise.backend.BaseIntegrationTest;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.user_service.utils.PasswordResetTokenUtils;

@AutoConfigureWebMvc
@Transactional
public class AuthIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepo;
    

    @Nested
    class ForgotPassword{
        @Test
        void shouldReturn200WhenEmailIsValid() throws Exception{
            // Arrange
            User user = new User();
            user.setEmailAddress("test@mock.com");
            user.setPassword("hashed-password");
            userRepo.save(user);

            // Act & Assert
            String jsonPayload = """
                    {
                        "emailAddress": "test@mock.com"
                    }
                    """;
            mockMvc.perform(post("/api/auth/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk());

            User updatedUser = userRepo.findByEmailAddress("test@mock.com").get();
            assertNotNull(updatedUser.getResetToken());
        }
    }

    @Nested
    class ResetPassword{
        User user = null;
        String token = "mock-token";
        String hashedToken = PasswordResetTokenUtils.hashToken(token);

        @BeforeEach
        void setup(){
            user = new User();
            user.setEmailAddress("test@mock.com");
            user.setPassword("old-password");
            user.setResetToken(hashedToken);
        }

        @Test
        void shouldReturn200andUpdatePasswordWhenTokenIsValid() throws Exception{
            // Arrange
            user.setResetTokenExpiry(Instant.now().plusSeconds(600));
            userRepo.save(user);

            // Act & Assert
            String jsonPayload = """
                    {
                        "token": "%s",
                        "password": "NewStrongPa$$123!"
                    }
                    """.formatted(token);
            mockMvc.perform(post("/api/auth/resetPassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isOk());

            User updatedUser = userRepo.findByEmailAddress("test@mock.com").get();
            
            assertNotEquals("old-password", updatedUser.getPassword());
            assertTrue(updatedUser.getPassword().startsWith("$2a$"), "Password should be BCrypt hashed");
            assertNull(updatedUser.getResetToken(), "Token should be cleared to prevent reuse");
            assertNull(updatedUser.getResetTokenExpiry());
        }

        @Test
        void shouldshouldReturn400WhenTokenIsExpired() throws Exception{
            // Arrange
            user.setResetTokenExpiry(Instant.now().minusSeconds(60));
            userRepo.save(user);

            // Act & Assert
            String jsonPayload = """
                    {
                        "token": "%s",
                        "password": "NewStrongPa$$123!"
                    }
                    """.formatted(token);
            mockMvc.perform(post("/api/auth/resetPassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());

            User updatedUser = userRepo.findByEmailAddress("test@mock.com").get();

            assertEquals("old-password", updatedUser.getPassword());
            assertNotNull(updatedUser.getResetToken());
        }

        @Test
        void shouldshouldReturn400WhenTokenIsInvalid() throws Exception{
            // Act & Assert
            String jsonPayload = """
                    {
                        "token": "%s",
                        "password": "short"
                    }
                    """.formatted(token);
            mockMvc.perform(post("/api/auth/resetPassword")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());
        }
    }
}
