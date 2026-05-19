package com.boardwise.backend.user_service.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.models.UserDetailImpl;
import com.boardwise.backend.user_service.services.AuthService;
import com.boardwise.backend.user_service.services.MyUserDetailsService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private MyUserDetailsService userDetailsService;

    @MockitoBean
    private JWTService jwt;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        
        when(userDetailsService.loadUserByUsername("testuser"))
            .thenReturn(new UserDetailImpl(testUser));
    }

    @Test
    @WithMockUser
    void shouldReturnHelloMessage() throws Exception {
        mockMvc.perform(get("/api/auth/hello"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRegisterUser_Successfully() throws Exception {
        String registerJson = """
        {
            "username": "testuser",
            "emailAddress": "test@example.com",
            "password": "StrongP@ss1!",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;
    
        when(authService.register(any()))
            .thenReturn(new AuthResponseDTO("User successfully register", "test.jwt.token"));

        mockMvc.perform(post("/api/auth/register")  // Changed from multipart to post
                .contentType(MediaType.APPLICATION_JSON)  // JSON content type
                .content(registerJson))  // Send JSON directly
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User successfully register"))
                .andExpect(jsonPath("$.accessToken").value("test.jwt.token"));
    }

    @Test
    void shouldReturn400_WhenRegisterDataInvalid() throws Exception {
        String invalidJson = """
        {
            "username": "ab",
            "emailAddress": "invalid-email",
            "password": "weak"
        }
        """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginUser_Successfully() throws Exception {
        String loginJson = """
            {
                "username": "testuser",
                "password": "StrongP@ss1!"
            }
            """;
        
        when(authService.login(any()))
            .thenReturn(new AuthResponseDTO("User logged in successfully", "test.jwt.token"));

        // Login endpoint is publicly accessible
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged in successfully"))
                .andExpect(jsonPath("$.accessToken").value("test.jwt.token"));
    }

    @Test
    void shouldReturn400_WhenLoginDataInvalid() throws Exception {
        String invalidLoginJson = """
            {
                "username": "testuser"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldLogoutUser_Successfully() throws Exception {
        when(authService.logout(anyString()))
            .thenReturn(new LogoutResponseDTO("User successfully logged out"));

        mockMvc.perform(delete("/api/auth/logout")
                .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User successfully logged out"));
        
        verify(authService).logout("test.jwt.token");
    }

    @Test
    void shouldReturn401_WhenNoAuthHeader() throws Exception {
        // This endpoint requires authentication per SecurityConfig
        mockMvc.perform(delete("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}