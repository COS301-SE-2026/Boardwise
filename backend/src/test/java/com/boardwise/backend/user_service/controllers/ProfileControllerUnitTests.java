package com.boardwise.backend.user_service.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.util.NoSuchElementException;

import org.bson.types.ObjectId;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.boardwise.backend.user_service.dtos.FriendDTO;
import com.boardwise.backend.user_service.dtos.FriendsListDTO;
import com.boardwise.backend.user_service.fixtures.ProfileServiceFixtures;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.models.UserDetailImpl;
import com.boardwise.backend.user_service.repository.TokenBlackListRepository;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.shared.config.SecurityConfig;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.security.JwtFilter;
import com.boardwise.backend.user_service.services.MyUserDetailsService;
import com.boardwise.backend.user_service.services.ProfileService;

@ActiveProfiles("test")
@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, JwtFilter.class})
public class ProfileControllerUnitTests {

    @MockitoBean
    private ProfileService service;

    @MockitoBean
    private JWTService jwtService;

    @MockitoBean
    private MyUserDetailsService userDetailsService;

    @MockitoBean
    private TokenBlackListRepository tokenBlackListRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Testing friends system endpoints")
    class FriendFunctionalityTests{
        
        private String fakeToken = "some-valid-token";
        private User testUser = ProfileServiceFixtures.owner();

        private FriendsListDTO getOwnFriendsListDTO(){ // Owner

            User friend1 = ProfileServiceFixtures.friend1();
            User friend2 = ProfileServiceFixtures.friend2();

            List<FriendDTO> friends = List.of(
                new FriendDTO(
                    friend1.getId(),
                    friend1.getUsername(),
                    friend1.getFirstName() + " " + friend1.getLastName(),
                    friend1.getProfilePicture()
                ),
                new FriendDTO(
                    friend2.getId(),
                    friend2.getUsername(),
                    friend2.getFirstName() + " " + friend2.getLastName(),
                    friend2.getProfilePicture()
                )
            );


            return new FriendsListDTO(
                "User friends list successfully retrieved",
                friends,
                null
            );
        }

        private FriendsListDTO getFriendsListDTO(){ // Owner

            User friend1 = ProfileServiceFixtures.friend1();
            User friend2 = ProfileServiceFixtures.friend2();

            List<FriendDTO> friends = List.of(
                new FriendDTO(
                    friend1.getId(),
                    friend1.getUsername(),
                    friend1.getFirstName() + " " + friend1.getLastName(),
                    friend1.getProfilePicture()
                ),
                new FriendDTO(
                    friend2.getId(),
                    friend2.getUsername(),
                    friend2.getFirstName() + " " + friend2.getLastName(),
                    friend2.getProfilePicture()
                )
            );


            return new FriendsListDTO(
                "User friends list successfully retrieved",
                friends,
                List.of(new FriendDTO(
                    friend1.getId(),
                    friend2.getUsername(),
                    friend1.getFirstName() + " " + friend1.getLastName(),
                    friend1.getProfilePicture()
                ))
            );
        }

        @BeforeEach
        void setUp() throws Exception{
            when(jwtService.extractUserId(fakeToken))
            .thenReturn(new ObjectId(testUser.getId()));

            UserDetails mockDeets = new UserDetailImpl(testUser);
            when(userDetailsService.loadUserByUserId(testUser.getId()))
                .thenReturn(mockDeets);

            when(jwtService.validateToken(fakeToken, mockDeets))
                .thenReturn(true);
        }

        @Test
        @WithMockUser
        @DisplayName("GET /api/users/friends returns 200 with Friends List")
        void getOwnFriendsListReturns200() throws Exception{
            when(service.getOwnFriendsList(anyString()))
                .thenReturn(getOwnFriendsListDTO());

            mockMvc.perform(get("/api/users/friends")
                    .header("Authorization", "Bearer " + fakeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User friends list successfully retrieved"))
                    .andExpect(jsonPath("$.friends").isArray())
                    .andExpect(jsonPath("$.mutuals").value(IsNull.nullValue()));
                    
        }

        @Test
        @WithMockUser
        @DisplayName("GET /api/users/{userId}/friends returns 200 with Friends List")
        void getOtherUserFriendsListReturns200() throws Exception{
            when(service.getUserFriendsList(anyString(), eq("some-existent-id")))
                .thenReturn(getFriendsListDTO());

            // Just pretend friend 3 is requesting owner's friends list
            mockMvc.perform(get("/api/users/some-existent-id/friends")
                    .header("Authorization", "Bearer " + fakeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("User friends list successfully retrieved"))
                    .andExpect(jsonPath("$.friends").isArray())
                    .andExpect(jsonPath("$.friends").isNotEmpty())
                    .andExpect(jsonPath("$.friends[0].id").value(ProfileServiceFixtures.FRIEND_ID1))
                    .andExpect(jsonPath("$.mutuals").isArray())
                    .andExpect(jsonPath("$.mutuals").isNotEmpty())
                    .andExpect(jsonPath("$.mutuals[0].id").value(ProfileServiceFixtures.FRIEND_ID1));
                    
        }

        @Test
        @WithMockUser
        @DisplayName("GET /api/users/{userId}/friends returns 404 with reason for failure message")
        void getOtherUserFriendsListReturns404() throws Exception{
            String fakeNonExistentId = "some-non-existent-id";
            when(service.getUserFriendsList(anyString(), eq(fakeNonExistentId)))
                .thenThrow(new NoSuchElementException("User associated with id: " + fakeNonExistentId + " does not exist."));

            // Just pretend friend 3 is requesting owner's friends list
            mockMvc.perform(get("/api/users/" + fakeNonExistentId + "/friends")
                    .header("Authorization", "Bearer " + fakeToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("User associated with id: " + fakeNonExistentId + " does not exist."));
                    
        }

    }
}
