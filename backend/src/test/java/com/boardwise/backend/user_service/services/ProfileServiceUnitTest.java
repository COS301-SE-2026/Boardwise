package com.boardwise.backend.user_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.FriendsListDTO;
import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.fixtures.ProfileServiceFixtures;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.FriendShipRepository;
import com.boardwise.backend.user_service.repos.GroupMembershipRepository;
import com.boardwise.backend.user_service.repos.GroupRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.google.maps.GeoApiContext;

@ExtendWith(MockitoExtension.class)
@DisplayName("Profile Service Unit Tests")
public class ProfileServiceUnitTest {
    
    @Mock
    private UserRepository userRepo;
    @Mock
    private JWTService jwtService;
    @Mock
    private FriendShipRepository fsRepo;
    @Mock
    private GroupMembershipRepository gmRepo;
    @Mock
    private GroupRepository groupRepo;
    @Mock
    private BoardGameRepository gameRepo;
    @Mock
    private R2StorageService bucket;
    @Mock
    private GeoApiContext geoContext;
    @Mock
    private MongoTemplate template;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ProfileService profileService;

    @Nested
    @DisplayName("Testing friends system functionality")
    class FriendFunctionalityTests{

        @Test
        @DisplayName("Should retrieve authenticated user's friends list as a FriendsListDTO with success message, populated friends array and null mutuals array.")
        void shouldReturnFriendListDTOWithSuccessMessageFriendsArrayAndNullMutualsArray(){
            // Arrange
            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(ProfileServiceFixtures.OWNER_ID));
            
            Mockito.when(fsRepo.findByUserAndStatus(anyString(), eq(FriendStatus.ACCEPTED)))
                    .thenReturn(List.of(
                        ProfileServiceFixtures.friendship1(),
                        ProfileServiceFixtures.friendship2()
                    ));

            Mockito.when(userRepo.findById(ProfileServiceFixtures.FRIEND_ID1))
                    .thenReturn(Optional.of(ProfileServiceFixtures.friend1()));

            Mockito.when(userRepo.findById(ProfileServiceFixtures.FRIEND_ID2))
                    .thenReturn(Optional.of(ProfileServiceFixtures.friend2()));
            
            // Act
            var result = profileService.getOwnFriendsList("");

            // Assert
            assertTrue(result instanceof FriendsListDTO);
            assertEquals(result.mutuals(), null);
            assertTrue(result.friends().size() > 0);
            assertTrue(result.friends().get(0).id().equals(ProfileServiceFixtures.FRIEND_ID1));
            assertEquals(result.message(), "User friends list successfully retrieved");
        }

        @Test
        @DisplayName("Should retrieve queried user's friends list with as a FriendsListDTO with success message, populated friends array and not null mutuals array.")
        void shouldReturnFriendListDTOWithSuccessMessageFriendsArrayAndNonNullMutualsArray(){
            // Arrange
            User friend3 = ProfileServiceFixtures.friend3(); // requested
            User friend2 = ProfileServiceFixtures.friend2(); // client
            Mockito.when(userRepo.existsById(friend3.getId()))
                    .thenReturn(true);

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend2.getId()));        
            

            // Act 

            // Assert
        }

        @Test
        @DisplayName("Should retrieve queried user's friends list with as a FriendsListDTO with success message, populated friends array and mutuals array of size 1.")
        void shouldReturnFriendListDTOWithSuccessMessageFriendsArrayAndSingletonMutualsArray(){
            // Arrange

            // Act 

            // Assert
        }
    }

}
