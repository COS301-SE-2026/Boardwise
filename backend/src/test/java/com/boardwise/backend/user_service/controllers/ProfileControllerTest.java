package com.boardwise.backend.user_service.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.boardwise.backend.shared.config.R2Config;
import com.boardwise.backend.shared.config.SecurityConfig;
import com.boardwise.backend.shared.security.JWTFilter;
import com.boardwise.backend.user_service.dtos.PreferencesRequestDTO;
import com.boardwise.backend.user_service.dtos.ProfilePictureResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
import com.boardwise.backend.user_service.dtos.UpdateProfileDTO;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.Preferences;
import com.boardwise.backend.user_service.services.JWTService;
import com.boardwise.backend.user_service.services.MyUserDetailsService;
import com.boardwise.backend.user_service.services.ProfileService;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.mockito.Mockito.doAnswer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;


@WebMvcTest(
    controllers = ProfileController.class,
    excludeAutoConfiguration = {R2Config.class}
) 
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProfileService profileService;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private MyUserDetailsService userDetailsService;

    @MockitoBean
    private AuthenticationProvider authProvider;

    @MockitoBean
    private AuthenticationManager authManager;

    private String testUsername = "testuser";
    private ProfileResponseDTO mockProfileResponse;
    private UpdateProfileDTO updateProfileDTO;
    private PreferencesRequestDTO preferencesRequestDTO;

    @BeforeEach
    void setUp() throws ServletException, IOException {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());

        when(jwtService.extractUsername(anyString())).thenReturn(testUsername);
        Preferences prefs = new Preferences(false, Arrays.asList("Strategy", "Adventure"));
        List<Boardgame> games = Arrays.asList();
        
        mockProfileResponse = new ProfileResponseDTO(
            testUsername,
            "https://r2.example.com/profile.jpg",
            5, 3, 10, games, prefs, "15-05-2026"
        );
        
        updateProfileDTO = new UpdateProfileDTO(
            "newusername", "NewP@ssw0rd", "newemail@test.com", prefs
        );
        
        preferencesRequestDTO = new PreferencesRequestDTO(
            true, Arrays.asList("Horror", "Cooperative")
        );

    }
        
    @Test
    @WithMockUser
    void shouldGetPublicProfile_Successfully() throws Exception {
        when(profileService.getProfile(testUsername)).thenReturn(mockProfileResponse);
        
        mockMvc.perform(get("/api/users/{username}", testUsername)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.friendCount").value(5))
                .andExpect(jsonPath("$.groupCount").value(3))
                .andExpect(jsonPath("$.ownedGameCount").value(10));
    }
    
    @Test
    @WithMockUser
    void shouldReturn404_WhenUserNotFound() throws Exception {
        String nonExistentUser = "nonexistent";
        when(profileService.getProfile(nonExistentUser))
            .thenThrow(new NoSuchElementException());
        
        mockMvc.perform(get("/api/users/{username}", nonExistentUser)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with that username does not exist."));
    }
    
    @Test
    @WithMockUser
    void shouldHandleServerError_WhenGettingProfile() throws Exception {
        when(profileService.getProfile(anyString()))
            .thenThrow(new RuntimeException("Database connection failed"));
        
        mockMvc.perform(get("/api/users/{username}", testUsername)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong on our end."));
    }

    
    @Test
    @WithMockUser
    void shouldGetOwnProfile_Successfully() throws Exception {
        when(profileService.getOwnProfile(anyString())).thenReturn(mockProfileResponse);
        
        mockMvc.perform(get("/api/users/")
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername));
    }
    
    @Test
    void shouldReturn401_WhenNoTokenForOwnProfile() throws Exception {
        mockMvc.perform(get("/api/users/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser
    void shouldReturn404_WhenTokenUserNotFound() throws Exception {
        when(profileService.getOwnProfile(anyString()))
            .thenThrow(new NoSuchElementException());
        
        mockMvc.perform(get("/api/users/")
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User of associated token does not exist."));
    }

    
    @Test
    @WithMockUser
    void shouldDeleteProfile_Successfully() throws Exception {
        when(profileService.deleteUser(anyString())).thenReturn(true);
        
        mockMvc.perform(delete("/api/users/")
                
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account deleted successfully."));
    }
    
    @Test
    @WithMockUser
    void shouldReturn500_WhenDeleteFails() throws Exception {
        when(profileService.deleteUser(anyString())).thenReturn(false);
        
        mockMvc.perform(delete("/api/users/")
                
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to delete account. Something went wrong on our side."));
    }

    
    @Test
    @WithMockUser
    void shouldUpdateProfile_Successfully() throws Exception {
        Map<String, Object> updateResponse = new HashMap<>();
        updateResponse.put("username", "newusername");
        updateResponse.put("email", "newemail@test.com");
        
        when(profileService.updateProfile(anyString(), any(UpdateProfileDTO.class)))
            .thenReturn(updateResponse);
        
        mockMvc.perform(patch("/api/users/") 
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andExpect(jsonPath("$.email").value("newemail@test.com"))
                .andDo(print());
    }
    
    @Test
    @WithMockUser
    void shouldUpdateProfile_WithPartialData() throws Exception {
        UpdateProfileDTO partialUpdate = new UpdateProfileDTO("newusername", null, null, null);
        
        Map<String, Object> updateResponse = new HashMap<>();
        updateResponse.put("username", "newusername");
        
        when(profileService.updateProfile(anyString(), any(UpdateProfileDTO.class)))
            .thenReturn(updateResponse);
        
        mockMvc.perform(patch("/api/users/")
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"))
                .andDo(print());
    }
    
    @Test
    @WithMockUser
    void shouldReturn500_WhenUpdateFails() throws Exception {
        when(profileService.updateProfile(anyString(), any(UpdateProfileDTO.class)))
            .thenThrow(new RuntimeException("Update failed"));
        
        mockMvc.perform(patch("/api/users/")
                
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProfileDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong during profile update."));
    }

    
    @Test
    @WithMockUser
    void shouldUpdateProfilePicture_Successfully() throws Exception {
        MockMultipartFile profilePic = new MockMultipartFile(
            "profilePicture", "avatar.jpg", "image/jpeg", "test image content".getBytes()
        );
        
        ProfilePictureResponseDTO picResponse = new ProfilePictureResponseDTO(
            "Profile picture successfully update", "https://r2.example.com/testuser/avatar.jpg"
        );
        
        when(profileService.changeProfilePicture(anyString(), any(MultipartFile.class)))
            .thenReturn(picResponse);
        
        mockMvc.perform(multipart("/api/users/profilePicture")
                .file(profilePic)
                
                .header("Authorization", "Bearer test.jwt.token")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Profile picture successfully update"))
                .andExpect(jsonPath("$.profilePictureUrl").exists())
                .andDo(print());
    }
    
    @Test
    @WithMockUser
    void shouldReturn500_WhenEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
            "profilePicture", "empty.jpg", "image/jpeg", new byte[0]
        );
        
        when(profileService.changeProfilePicture(anyString(), any(MultipartFile.class)))
            .thenThrow(new RuntimeException("File is empty"));
        
        mockMvc.perform(multipart("/api/users/profilePicture")
                .file(emptyFile)
                
                .header("Authorization", "Bearer test.jwt.token")
                .with(request -> {
                    request.setMethod("POST");
                    return request;
                }))
                .andExpect(status().isInternalServerError());
    }

    
    @Test
    @WithMockUser
    void shouldUpdatePreferences_Successfully() throws Exception {
        Map<String, Object> prefResponse = new HashMap<>();
        prefResponse.put("message", "Preferences updated successfully.");
        prefResponse.put("preferences", Map.of(
            "isPrivate", true,
            "genres", Arrays.asList("Horror", "Cooperative")
        ));
        
        when(profileService.updateOrSetPreferences(anyString(), any(PreferencesRequestDTO.class)))
            .thenReturn(prefResponse);
        
        mockMvc.perform(put("/api/users/preferences")
                
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferencesRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Preferences updated successfully."))
                .andExpect(jsonPath("$.preferences.isPrivate").value(true))
                .andExpect(jsonPath("$.preferences.genres.length()").value(2));
    }
    
    @Test
    @WithMockUser
    void shouldUpdatePreferences_WithNullGenres() throws Exception {
        PreferencesRequestDTO nullGenresRequest = new PreferencesRequestDTO(true, null);
        
        Map<String, Object> prefResponse = new HashMap<>();
        prefResponse.put("message", "Preferences updated successfully.");
        prefResponse.put("preferences", Map.of("isPrivate", true, "genres", Arrays.asList()));
        
        when(profileService.updateOrSetPreferences(anyString(), any(PreferencesRequestDTO.class)))
            .thenReturn(prefResponse);
        
        mockMvc.perform(put("/api/users/preferences")
                
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullGenresRequest)))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser
    void shouldReturn500_WhenPreferenceUpdateFails() throws Exception {
        when(profileService.updateOrSetPreferences(anyString(), any(PreferencesRequestDTO.class)))
            .thenThrow(new RuntimeException("Database error"));
        
        mockMvc.perform(put("/api/users/preferences")
                
                .header("Authorization", "Bearer test.jwt.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferencesRequestDTO)))
                .andExpect(status().isInternalServerError());
    }
}