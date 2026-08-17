package com.boardwise.backend.user_service.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.FriendDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestsDTO;
import com.boardwise.backend.user_service.dtos.FriendsListDTO;
import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.fixtures.ProfileServiceFixtures;
import com.boardwise.backend.user_service.models.Friendship;
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
        @DisplayName("Should retrieve queried user's friends list as a FriendsListDTO with success message, populated friends array and not null mutuals array.")
        void shouldReturnFriendListDTOWithSuccessMessageFriendsArrayAndNonNullMutualsArray(){
            // Arrange
            User friend3 = ProfileServiceFixtures.friend3(); // requested
            User friend2 = ProfileServiceFixtures.friend2(); // client
            List<Friendship> f3Friendships = List.of(ProfileServiceFixtures.friendship3());
            List<Friendship> f2Friendships = List.of(ProfileServiceFixtures.friendship2());

            Mockito.when(userRepo.existsById(friend3.getId()))
                    .thenReturn(true);

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend2.getId())); // client       
            
            Mockito.when(fsRepo.findByUserAndStatus(friend3.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(f3Friendships);

            Mockito.when(fsRepo.findByUserAndStatus(friend2.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(f2Friendships);

            // You have to mock the call to userRepo INSIDE the private method
            Mockito.when(userRepo.findById(ProfileServiceFixtures.FRIEND_ID1))
                    .thenReturn(Optional.of(ProfileServiceFixtures.friend1()));
            
            Mockito.when(userRepo.findById(ProfileServiceFixtures.OWNER_ID))
                    .thenReturn(Optional.of(ProfileServiceFixtures.owner()));
            // Act 
            FriendsListDTO result = profileService.getUserFriendsList(anyString(), friend3.getId());

            // Assert
            assertTrue(result.friends() != null);
            assertTrue(result.mutuals() != null);
            assertTrue(result.mutuals().size() == 0); // should be zero
            assertEquals(1, result.friends().size()); // should be one tho
            assertEquals(ProfileServiceFixtures.FRIEND_ID1, result.friends().get(0).id());
            assertEquals("User friends list successfully retrieved", result.message());
        }

        @Test
        @DisplayName("Should retrieve queried user's friends list as a FriendsListDTO with success message, populated friends array and mutuals array of size 1.")
        void shouldReturnFriendListDTOWithSuccessMessageFriendsArrayAndSingletonMutualsArray(){
            // Arrange
            User owner = ProfileServiceFixtures.owner(); // requested
            User friend3 = ProfileServiceFixtures.friend3(); // client
            List<Friendship> f3Friendships = List.of(ProfileServiceFixtures.friendship3());
            List<Friendship> oFriendships = List.of(ProfileServiceFixtures.friendship1());

            Mockito.when(userRepo.existsById(owner.getId()))
                    .thenReturn(true);

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend3.getId())); // client       
            
            Mockito.when(fsRepo.findByUserAndStatus(owner.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(oFriendships);

            Mockito.when(fsRepo.findByUserAndStatus(friend3.getId(), FriendStatus.ACCEPTED))
                    .thenReturn(f3Friendships);

            // You have to mock the call to userRepo INSIDE the private method
            Mockito.when(userRepo.findById(ProfileServiceFixtures.FRIEND_ID1))
                    .thenReturn(Optional.of(ProfileServiceFixtures.friend1()));

            // Act 
            FriendsListDTO result = profileService.getUserFriendsList(anyString(), owner.getId());

            // Assert
            assertTrue(result.friends() != null);
            assertTrue(result.mutuals() != null);
            assertEquals(1, result.mutuals().size());
            assertEquals(1, result.friends().size());
            assertEquals(ProfileServiceFixtures.FRIEND_ID1, result.friends().get(0).id());
            assertEquals(ProfileServiceFixtures.FRIEND_ID1, result.mutuals().get(0).id());
            assertEquals("User friends list successfully retrieved", result.message());

        }
        
        @Test
        @DisplayName("Should attempt to retrieve a non-existent user's friends list and throw a NoSuchElementException")
        void shouldThrowANoSuchElementException(){
            // Arrange
            Mockito.when(userRepo.existsById(anyString())).thenReturn(false);

            // Act & assert (same step because of how the assertion needs to be written)
            assertThrows(NoSuchElementException.class, () -> profileService.getUserFriendsList("", ""));
        }
        
        @Test
        @DisplayName("Should retrieve authenticated user's friend requests as a FriendRequestsDTO with a success message and requests array")
        void shouldReturnAFriendRequestsDTOWithSuccessMessageAndFriendRequestsArray(){
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            List<Friendship> f2Friendships = List.of(
                ProfileServiceFixtures.friendship4(),
                ProfileServiceFixtures.friendship5()
            );

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend2.getId()));

            Friendship fs = new Friendship();
            fs.setReceiver(friend2.getId());
            fs.setStatus(FriendStatus.REQUESTED);
            Example<Friendship> example = Example.of(fs);
            Mockito.when(fsRepo.findAll(example))
                    .thenReturn(f2Friendships);
            
            Mockito.when(userRepo.findById(ProfileServiceFixtures.FRIEND_ID1))
                    .thenReturn(Optional.of(ProfileServiceFixtures.friend1()));

            Mockito.when(userRepo.findById(ProfileServiceFixtures.FRIEND_ID3))
                    .thenReturn(Optional.of(ProfileServiceFixtures.friend3()));
            // Act
            FriendRequestsDTO result = profileService.getFriendRequests("");

            // Assert
            assertEquals("User friend request successfully retrieved", result.message());
            assertEquals(2, result.requests().size());
            assertEquals("fs-004", result.requests().get(0).id());
            assertEquals("fs-005", result.requests().get(1).id());

            FriendDTO senderOfIndex0 = result.requests().get(0).sender();
            assertEquals(ProfileServiceFixtures.FRIEND_ID1, senderOfIndex0.id());

            FriendDTO senderOfIndex1 = result.requests().get(1).sender();
            assertEquals(ProfileServiceFixtures.FRIEND_ID3, senderOfIndex1.id());
        }
        
    }

}
