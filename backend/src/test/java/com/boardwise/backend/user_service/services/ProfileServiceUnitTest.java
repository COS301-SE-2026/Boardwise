package com.boardwise.backend.user_service.services;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.FriendConfirmationNotification;
import com.boardwise.backend.user_service.dtos.FriendDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestNotification;
import com.boardwise.backend.user_service.dtos.FriendRequestResponseDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestsDTO;
import com.boardwise.backend.user_service.dtos.FriendsListDTO;
import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.fixtures.ProfileServiceFixtures;
import com.boardwise.backend.user_service.models.Friendship;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.user_service.repository.FriendShipRepository;
import com.boardwise.backend.user_service.repository.GroupMembershipRepository;
import com.boardwise.backend.user_service.repository.GroupRepository;
import com.boardwise.backend.user_service.repository.UserRepository;
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
        void shouldAttemptToRetrieveANonExistentUsersFriendsListAndThrowANoSuchElementException(){
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
        
        @Test
        @DisplayName("Should send a friend request and respond with success message for request being sent")
        void shouldReturnAFriendRequestResponseDTOWithSuccessMessage() throws IllegalAccessException, IllegalArgumentException, NoSuchElementException{
            // Arrange
            User friend = ProfileServiceFixtures.owner(); // clientId
            User friend3 = ProfileServiceFixtures.friend3(); // userId

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend.getId()));

            Mockito.when(userRepo.findById(friend.getId()))
                    .thenReturn(Optional.of(friend));

            Mockito.when(userRepo.existsById(friend3.getId()))
                    .thenReturn(true);

            Mockito.when(fsRepo.findFriendShipBetweenUsers(friend3.getId(), friend.getId()))
                    .thenReturn(Optional.empty());
            
            Friendship fs = new Friendship(friend.getId(), friend3.getId());
            fs.setId("fs-006");
            Mockito.when(fsRepo.save(any())).thenReturn(fs);

            // Act 
            FriendRequestResponseDTO result = profileService.sendFriendRequest("", friend3.getId());

            // Assert
            ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
            verify(fsRepo, times(1)).save(captor.capture());
            Friendship saved = captor.getValue();
            assertEquals(friend.getId(), saved.getSender());
            assertEquals(friend3.getId(), saved.getReceiver());
            assertEquals(FriendStatus.REQUESTED, saved.getStatus());

            verify(fsRepo, times(1)).findFriendShipBetweenUsers(friend3.getId(), friend.getId());

            ArgumentCaptor<FriendRequestNotification> frCaptor = ArgumentCaptor.forClass(FriendRequestNotification.class);
            verify(notificationService, times(1)).notifyUser(eq(friend3.getId()), frCaptor.capture());
            FriendRequestNotification savedNotification = frCaptor.getValue();
            assertEquals("FRIEND_REQUEST", savedNotification.type());
            assertEquals("fs-006", savedNotification.request().id());
            assertEquals(ProfileServiceFixtures.OWNER_ID, savedNotification.request().sender().id());

            assertEquals("Friend request successfully sent.", result.message());
        
        }
        
        @Test
        @DisplayName("Should send a friend request and respond with success message for request being sent after operating on an existing friendship")
        void shouldReturnAFriendRequestResponseDTOWithSuccessMessageViaExistingFriendship() throws IllegalAccessException, IllegalArgumentException, NoSuchElementException{
            // Arrange
            User friend = ProfileServiceFixtures.owner(); // clientId
            User friend3 = ProfileServiceFixtures.friend3(); // userId

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend.getId()));

            Mockito.when(userRepo.findById(friend.getId()))
                    .thenReturn(Optional.of(friend));

            Mockito.when(userRepo.existsById(friend3.getId()))
                    .thenReturn(true);

            Friendship fs = new Friendship(friend.getId(), friend3.getId());
            fs.setId("fs-005");
            fs.setStatus(FriendStatus.DECLINED);
            Mockito.when(fsRepo.findFriendShipBetweenUsers(friend3.getId(), friend.getId()))
                    .thenReturn(Optional.of(fs));

            Mockito.when(fsRepo.save(any())).thenReturn(fs);

            // Act 
            FriendRequestResponseDTO result = profileService.sendFriendRequest("", friend3.getId());

            // Arrange
            ArgumentCaptor<FriendRequestNotification> frCaptor = ArgumentCaptor.forClass(FriendRequestNotification.class);
            verify(notificationService, times(1)).notifyUser(eq(friend3.getId()), frCaptor.capture());
            FriendRequestNotification savedNotification = frCaptor.getValue();
            assertEquals("FRIEND_REQUEST", savedNotification.type());
            assertEquals("fs-005", savedNotification.request().id());
            assertEquals(ProfileServiceFixtures.OWNER_ID, savedNotification.request().sender().id());
            
            ArgumentCaptor<Friendship> captor = ArgumentCaptor.forClass(Friendship.class);
            verify(fsRepo, times(1)).save(captor.capture());
            Friendship saved = captor.getValue();
            assertEquals(friend.getId(), saved.getSender());
            assertEquals(friend3.getId(), saved.getReceiver());
            assertEquals(FriendStatus.REQUESTED, saved.getStatus());

            assertEquals("Friend request successfully sent.", result.message());
            
        }
    
        @Test
        @DisplayName("Should attempt to send a friend request to a non-existent user and throw a NoSuchElementException")
        void shouldAttemptToSendFriendRequestToNonExistentUserAndThrowANoSuchElementException(){
            // Arrange
            User friend = ProfileServiceFixtures.owner();
            String nonExistentId = "507f1f77bcf86cd799439015";
            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend.getId()));

            Mockito.when(userRepo.findById(friend.getId()))
                    .thenReturn(Optional.of(friend));

            Mockito.when(userRepo.existsById(anyString()))
                    .thenReturn(false);

            
            // Act & Assert
            assertThatThrownBy(() -> profileService.sendFriendRequest("", nonExistentId))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessage("User associated with id: " + nonExistentId + " does not exist.");
        }

        @Test
        @DisplayName("Should attempt to send a friend request to self and throw a IllegalArgumentException")
        void shouldAttemptToSendFriendRequestToSelfndThrowAnIllegalArgumentException(){
            // Arrange
            User friend = ProfileServiceFixtures.owner();
            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend.getId()));

            Mockito.when(userRepo.findById(friend.getId()))
                    .thenReturn(Optional.of(friend));

            Mockito.when(userRepo.existsById(friend.getId()))
                    .thenReturn(true);
            
            // Act & Assert
            assertThatThrownBy(() -> profileService.sendFriendRequest("null", friend.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Users cannot send friend requests to themselves.");
    
        }
        
        @Test
        @DisplayName("Should attempt to send a friend request to a user with whom already friends and throw an IllegalAccessException")
        void shouldAttemptToSendFriendRequestToUserWithWhomAlreadyFriendsAndThrowAnIllegalAccessException(){
            // Arrange
            Friendship fs = ProfileServiceFixtures.friendship1();
            User owner = ProfileServiceFixtures.owner();
            User friend1 = ProfileServiceFixtures.friend1();

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(owner.getId()));
            
            Mockito.when(userRepo.findById(owner.getId()))
                    .thenReturn(Optional.of(owner));

            Mockito.when(userRepo.existsById(friend1.getId()))
                    .thenReturn(true);
            
            Mockito.when(fsRepo.findFriendShipBetweenUsers(friend1.getId(), owner.getId()))
                    .thenReturn(Optional.of(fs));

            // Act && Assert
            assertThatThrownBy(() -> profileService.sendFriendRequest("null", friend1.getId()))
                    .isInstanceOf(IllegalAccessException.class)
                    .hasMessage("User is already friends with user of id: " + friend1.getId() + ".");
        }

        @Test
        @DisplayName("Should attempt to send a friend request to a user to whom a friend request was already sent to and throw an IllegalAccessException")
        void shouldAttemptToSendFriendRequestToUserToWhomAFriendRequestHasBeenSentToAndThrowAnIllegalAccessException(){
            // Arrange
            Friendship fs = ProfileServiceFixtures.friendship4();
            User friend1 = ProfileServiceFixtures.friend1();
            User friend2 = ProfileServiceFixtures.friend2();

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend1.getId()));
            
            Mockito.when(userRepo.findById(friend1.getId()))
                    .thenReturn(Optional.of(friend1));

            Mockito.when(userRepo.existsById(friend2.getId()))
                    .thenReturn(true);
            
            Mockito.when(fsRepo.findFriendShipBetweenUsers(friend2.getId(), friend1.getId()))
                    .thenReturn(Optional.of(fs));

            // Act && Assert
            assertThatThrownBy(() -> profileService.sendFriendRequest("null", friend2.getId()))
                    .isInstanceOf(IllegalAccessException.class)
                    .hasMessage("User already sent a request to user associated with id: " + friend2.getId() + ".");

        }

        @Test
        @DisplayName("Should attempt to send a friend request to a user to whom a friend request was already received from and throw an IllegalAccessException")
        void shouldAttemptToSendFriendRequestToUserToWhomAFriendRequestExistsFromAndThrowAnIllegalAccessException(){
            // Arrange
            Friendship fs = ProfileServiceFixtures.friendship4();
            User friend1 = ProfileServiceFixtures.friend1();
            User friend2 = ProfileServiceFixtures.friend2();

            Mockito.when(jwtService.extractUserId(anyString()))
                    .thenReturn(new ObjectId(friend2.getId()));
            
            Mockito.when(userRepo.findById(friend2.getId()))
                    .thenReturn(Optional.of(friend2));

            Mockito.when(userRepo.existsById(friend1.getId()))
                    .thenReturn(true);
            
            Mockito.when(fsRepo.findFriendShipBetweenUsers(friend1.getId(), friend2.getId()))
                    .thenReturn(Optional.of(fs));

            // Act && Assert
            assertThatThrownBy(() -> profileService.sendFriendRequest("null", friend1.getId()))
                    .isInstanceOf(IllegalAccessException.class)
                    .hasMessage("User has a request from user associated with id: " + friend1.getId() + ".");
        }
    
        @Test
        @DisplayName("Should respond to a received friend request by accepting it and should occur successfully")
        void shouldRespondToAFriendRequestWithAcceptSuccessfully() throws IllegalAccessException, IllegalArgumentException, NoSuchElementException{
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            User friend1 = ProfileServiceFixtures.friend1();
            Friendship fs = ProfileServiceFixtures.friendship4();

            Mockito.when(fsRepo.findById(fs.getId()))   
                    .thenReturn(Optional.of(fs));

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(friend2.getId()));

            Mockito.when(userRepo.findById(friend2.getId()))   
                    .thenReturn(Optional.of(friend2));

            Friendship newFs = ProfileServiceFixtures.friendship4();
            newFs.setStatus(FriendStatus.ACCEPTED);
            Mockito.when(fsRepo.save(fs))
                    .thenReturn(newFs);
            
            // Act
            FriendRequestResponseDTO result = profileService.respondToFriendRequest("some.valid-token.for-friend2", fs.getId(), "accept");

            // Assert
            assertEquals("Friend request response successfully recorded.", result.message());

            ArgumentCaptor<FriendConfirmationNotification> notificationCaptor = ArgumentCaptor.forClass(FriendConfirmationNotification.class);
            verify(notificationService, times(1)).notifyUser(eq(friend1.getId()), notificationCaptor.capture());
            FriendConfirmationNotification captured = notificationCaptor.getValue();
            assertEquals("FRIEND_CONFIRMATION", captured.type());
            assertEquals(ProfileServiceFixtures.FRIEND_ID2, captured.friend().id());
            
        }

        @Test
        @DisplayName("Should respond to a received friend request by declining it and should occur successfully")
        void shouldRespondToAFriendRequestWithDeclineSuccessfully() throws IllegalAccessException, IllegalArgumentException, NoSuchElementException{
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            User friend1 = ProfileServiceFixtures.friend1();
            Friendship fs = ProfileServiceFixtures.friendship4();

            Mockito.when(fsRepo.findById(fs.getId()))   
                    .thenReturn(Optional.of(fs));

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(friend2.getId()));

            Mockito.when(userRepo.findById(friend2.getId()))   
                    .thenReturn(Optional.of(friend2));

            Friendship newFs = ProfileServiceFixtures.friendship4();
            newFs.setStatus(FriendStatus.DECLINED);
            Mockito.when(fsRepo.save(fs))
                    .thenReturn(newFs);
            
            // Act
            FriendRequestResponseDTO result = profileService.respondToFriendRequest("some.valid-token.for-friend2", fs.getId(), "decline");

            // Assert
            assertEquals("Friend request response successfully recorded.", result.message());
            verify(notificationService, times(0)).notifyUser(eq(friend1.getId()), any());
            
        }
        
        @Test
        @DisplayName("Should respond to a received friend request with an invalid status and throw an IllegalArgumentException")
        void shouldRespondToAFriendRequestWithInvalidStatus() {
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            Friendship fs = ProfileServiceFixtures.friendship4();

            Mockito.when(fsRepo.findById(fs.getId()))   
                    .thenReturn(Optional.of(fs));

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(friend2.getId()));

            Mockito.when(userRepo.findById(friend2.getId()))   
                    .thenReturn(Optional.of(friend2));
 
            // Act & Assert
            assertThatThrownBy(() -> profileService.respondToFriendRequest("some.valid-token.for-friend2", fs.getId(), "yes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Friend Request response status must be either \"accept\" or \"decline\".");
            
        }
        
        @Test
        @DisplayName("Should attempt to respond to request that already has response status (Accepted or Declined) and throw an IllegalAccessException")
        void shouldAttemptToRespondToFriendRequestThatAlreadyHasAResponseAndThrowIllegalAccessException() {
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            Friendship fs = ProfileServiceFixtures.friendship4();
            fs.setStatus(FriendStatus.DECLINED);

            Mockito.when(fsRepo.findById(fs.getId()))   
                    .thenReturn(Optional.of(fs));

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(friend2.getId()));

            Mockito.when(userRepo.findById(friend2.getId()))   
                    .thenReturn(Optional.of(friend2));

            // Act && assert
            assertThatThrownBy(() -> profileService.respondToFriendRequest("null", fs.getId(), ""))
                .isInstanceOf(IllegalAccessException.class)
                .hasMessage("Friend request with id: " + fs.getId() + " already has a response.");

        }

        @Test
        @DisplayName("Should attempt to respond to request that user is not the receiver of and throw an IllegalAccessException")
        void shouldAttemptToRespondToFriendRequestThatUserIsNotReceiverOfAndThrowIllegalAccessException() {
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            Friendship fs = ProfileServiceFixtures.friendship2();

            Mockito.when(fsRepo.findById(fs.getId()))   
                    .thenReturn(Optional.of(fs));

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(friend2.getId()));

            Mockito.when(userRepo.findById(friend2.getId()))   
                    .thenReturn(Optional.of(friend2));

            // Act && assert
            assertThatThrownBy(() -> profileService.respondToFriendRequest("null", fs.getId(), ""))
                .isInstanceOf(IllegalAccessException.class)
                .hasMessage("Friend request with id: " + fs.getId() + " was not sent to the requesting user (client).");

        }

        @Test
        @DisplayName("Should attempt to respond to request that is non-existent and throw a NoSuchElementException")
        void shouldAttemptToRespondToFriendRequestThatIsNonExistentAndThrowIllegalAccessException() {
            // Arrange
            User friend2 = ProfileServiceFixtures.friend2();
            String nonexistentId = "fs-999";
     
            Mockito.when(fsRepo.findById(nonexistentId))   
                    .thenReturn(Optional.empty());

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(friend2.getId()));

            Mockito.when(userRepo.findById(friend2.getId()))   
                    .thenReturn(Optional.of(friend2));

            // Act && assert
            assertThatThrownBy(() -> profileService.respondToFriendRequest("null", nonexistentId, ""))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Friend request with id: " + nonexistentId + " does not exist.");

        }
        
        @Test
        @DisplayName("Should unfriend a user and return a FriendRequestResponseDTO with a success message")
        void shouldUnfriendAUserAndReturnAFriendRequestDTOWithSuccessMessage() throws NoSuchElementException, IllegalAccessException{
            // Arrange 
            User owner = ProfileServiceFixtures.owner(); // cl
            String toUnfriend = ProfileServiceFixtures.FRIEND_ID1;
            Friendship fs = ProfileServiceFixtures.friendship1();

            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(owner.getId()));

            Mockito.when(userRepo.findById(owner.getId()))   
                    .thenReturn(Optional.of(owner));

            Mockito.when(userRepo.existsById(toUnfriend))
                    .thenReturn(true);

            Mockito.when(fsRepo.findFriendShipBetweenUsers(owner.getId(), toUnfriend))
                    .thenReturn(Optional.of(fs));
            
            // Act
            FriendRequestResponseDTO result = profileService.unfriendUser("", toUnfriend);

            // Assert
            assertEquals("Unfriend user query successful.", result.message());
            verify(fsRepo, times(1)).save(any());
        }

        @Test
        @DisplayName("Should attempt to unfriend a Non-existent user and throw a NoSuchElementException")
        void shouldAttemptToUnfriendNonExistentUserAndThrowANoSuchElementException() {
            // Arrange
            User owner = ProfileServiceFixtures.owner(); // cl
            String toUnfriend = "507f1f77bcf86cd799439100";
        
            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(owner.getId()));

            Mockito.when(userRepo.findById(owner.getId()))   
                    .thenReturn(Optional.of(owner));

            Mockito.when(userRepo.existsById(toUnfriend))
                    .thenReturn(false);

    
            // Act & Assert
            assertThatThrownBy(() -> profileService.unfriendUser("toUnfriend", toUnfriend))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("User with id: " + toUnfriend + " does not exist.");

        }

        @Test
        @DisplayName("Should attempt to unfriend a user with whom the client has no associated Friendship record and throw an IllegalAccessException")
        void shouldAttemptToUnfriendAUserTheClientHasNoAssociatedFriendshipWithAndThrowAnIllegalAccessException(){
            // Arrange
            User owner = ProfileServiceFixtures.owner(); // cl
            String toUnfriend = ProfileServiceFixtures.FRIEND_ID3;
        
            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(owner.getId()));

            Mockito.when(userRepo.findById(owner.getId()))   
                    .thenReturn(Optional.of(owner));

            Mockito.when(userRepo.existsById(toUnfriend))
                    .thenReturn(true);
            
            Mockito.when(fsRepo.findFriendShipBetweenUsers(owner.getId(), toUnfriend))
                    .thenReturn(Optional.empty());
    
            // Act & Assert
            assertThatThrownBy(() -> profileService.unfriendUser("toUnfriend", toUnfriend))
                .isInstanceOf(IllegalAccessException.class)
                .hasMessage("Requesting user is a not friends with the user associated with id: " + toUnfriend + ".");

        }

        @Test
        @DisplayName("Should attempt to unfriend a user with whom the client is not friends with yet and throw an IllegalAccessException")
        void shouldAttemptToUnfriendAUserTheClientIsNotFriendsWithAndThrowAnIllegalAccessException(){
            // Arrange
            User owner = ProfileServiceFixtures.owner(); // cl
            String toUnfriend = ProfileServiceFixtures.FRIEND_ID3;
            Friendship fs = new Friendship(owner.getId(), toUnfriend);
        
            Mockito.when(jwtService.extractUserId(anyString()))   
                    .thenReturn(new ObjectId(owner.getId()));

            Mockito.when(userRepo.findById(owner.getId()))   
                    .thenReturn(Optional.of(owner));

            Mockito.when(userRepo.existsById(toUnfriend))
                    .thenReturn(true);
            
            Mockito.when(fsRepo.findFriendShipBetweenUsers(owner.getId(), toUnfriend))
                    .thenReturn(Optional.of(fs));
    
            // Act & Assert
            assertThatThrownBy(() -> profileService.unfriendUser("toUnfriend", toUnfriend))
                .isInstanceOf(IllegalAccessException.class)
                .hasMessage("Requesting user is a not friends with the user associated with id: " + toUnfriend + ".");

        }
    }

}
