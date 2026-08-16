package com.boardwise.backend.user_service.services;


import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.FriendDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestResponseDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestsDTO;
import com.boardwise.backend.user_service.dtos.FriendsListDTO;
import com.boardwise.backend.user_service.dtos.GameInventoryDTO;
import com.boardwise.backend.user_service.dtos.Notification;
import com.boardwise.backend.user_service.dtos.OtherGameDTO;
import com.boardwise.backend.user_service.dtos.PreferencesRequestDTO;
import com.boardwise.backend.user_service.dtos.ProfilePictureResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileSearchResponse;
import com.boardwise.backend.user_service.dtos.UpdateProfileDTO;
import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.models.*;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.FriendShipRepository;
import com.boardwise.backend.user_service.repos.GroupMembershipRepository;
import com.boardwise.backend.user_service.repos.GroupRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepo;
    private final JWTService jwtService;
    private final FriendShipRepository fsRepo;
    private final GroupMembershipRepository gmRepo;
    private final GroupRepository groupRepo;
    private final BoardGameRepository gameRepo;
    private final R2StorageService bucket;
    private final GeoApiContext geoContext;
    private final MongoTemplate template;
    private final NotificationService notificationService;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ProfileResponseDTO getOwnProfile(String token) {
        // get user id from token
        ObjectId userId = jwtService.extractUserId(token);
        return getProfile(userId.toString());
    }

    public ProfileResponseDTO getProfile(String userId) {
        // get user data from db
        User user = userRepo.findById(userId).get();
        
        // get the games from stored ids                                
        List<GameInventoryDTO> games = new ArrayList<>();
        int ownedGameCount = user.getOwnedGames().size();
        for(String gameId : user.getOwnedGames()){
            Boardgame game = gameRepo.findById(gameId).get();
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getImageURL(),
                game.getGenres()
            );
            games.add(dto);
        }

        // get group count
        GroupMembership gm = new GroupMembership();
        gm.setUserId(user.getId());
        List<GroupMembership> gms = gmRepo.findAll(Example.of(gm));
        int groupCount = gms.size();
        
        // Get community id, name and image
        List<Map<String, String>> communities = new ArrayList<>();
        for(GroupMembership membership : gms){
            Map<String, String> community = new HashMap<>();
            Group group = groupRepo.findById(membership.getGroupId()).get();
            community.put("id", group.getId());
            community.put("name", group.getName());
            community.put("image", group.getImageUrl());

            communities.add(community);
        }
        
        // get friend count
        int friendCount = fsRepo.findByUserAndStatus(userId, FriendStatus.ACCEPTED).size();

        DateTimeFormatter formatter = DateTimeFormatter
                                        .ofPattern("dd-MM-yyyy")
                                        .withZone(ZoneOffset.UTC);

        Preferences userPref = user.getPreferences();
        String fullName = user.getFirstName() + " " + user.getLastName();                 
        return new ProfileResponseDTO(
            user.getId(),
            fullName,
            user.getUsername(),
            user.getLocation(),
            user.getProfilePicture(),
            friendCount,
            groupCount,
            ownedGameCount,
            games,
            communities,
            userPref,
            formatter.format(user.getCreatedAt())
        );
    }

    public List<ProfileSearchResponse> searchForUsers(String query, String token){
        List<ProfileSearchResponse> results = new ArrayList<>();
        String userId = jwtService.extractUserId(token).toString();
        User subject = userRepo.findById(userId).get();

        String cleanQuery = AuthService.sanitize(query);
        Criteria searchCriteria = Criteria.where("username").regex(cleanQuery, "i");
        Query dbQuery = new Query(searchCriteria);
        List<User> matches = template.find(dbQuery, User.class);

        for(User user : matches){
            if(!user.getId().equals(subject.getId()))
                results.add(new ProfileSearchResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getProfilePicture()
                    )
                );
        }

        return results;
    }

    public void deleteUser(String token) {
        String userId = jwtService.extractUserId(token).toString();
        // TODO: add removal of associated data

        userRepo.deleteById(userId);
    }

    public Map<String, Object> updateProfile(String token, UpdateProfileDTO profileUpdateData) throws ApiException, InterruptedException, IOException, NoSuchElementException {
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findById(userId).get();

        String newFirstName = AuthService.sanitize(profileUpdateData.firstName());
        String newLastName = AuthService.sanitize(profileUpdateData.lastName());
        String newUsername = AuthService.sanitize(profileUpdateData.username());
        String newEmail = AuthService.sanitize(profileUpdateData.emailAddress());
        String newPassword = (profileUpdateData.password() != null) ?
                                encoder.encode(profileUpdateData.password()) :
                                null;
        String newLocation = AuthService.sanitize(profileUpdateData.location());

        Map<String, Object> toReturn = new HashMap<>();
        
        if(newFirstName != null && newLastName != null){
            user.setFirstName(newFirstName);
            user.setLastName(newLastName);
            toReturn.put("fullName", newFirstName + " " + newLastName);
        }

        if(newUsername != null){
            user.setUsername(newUsername);
            toReturn.put("username", newUsername);
        }
        
        if(newLocation != null){
            GeocodingResult[] results = GeocodingApi.geocode(geoContext, newLocation).await();
            if(results.length == 0)
                throw new NoSuchElementException("Could not find coordinates for location: " + newLocation);

            user.setLocation(newLocation);
            toReturn.put("location", newLocation);
        }

        if(newEmail != null){
            user.setEmailAddress(newEmail);
            toReturn.put("email", newEmail);
        }
            
        if(newPassword != null){
            user.setPassword(newPassword);
            toReturn.put("passwordMessage", "Password successfully updated.");
        }

        if(profileUpdateData.preferences() != null){
            String visibility = profileUpdateData.preferences().getVisibility() == null ? 
                                user.getPreferences().getVisibility() : 
                                profileUpdateData.preferences().getVisibility();

            PreferencesRequestDTO dto = new PreferencesRequestDTO(
                visibility,
                profileUpdateData.preferences().getGenres()
            );
            Map<String, Object> prefs = updateOrSetPreferences(token, dto);
            toReturn.put("preferences", prefs.get("preferences"));
        }

        userRepo.save(user);
        
        return toReturn;
    }

    public ProfilePictureResponseDTO changeProfilePicture(String token, MultipartFile pfp) throws IOException {
        String url = "";
        String message = "";
        String userId = jwtService.extractUserId(token).toString();

        // logic here
        String fileName = bucket.uploadFile(pfp, userId);
        url = bucket.getFileUrl(fileName);
        message = "Profile picture successfully update";
         
        User user = userRepo.findById(userId).get();
        user.setProfilePicture(url);
        userRepo.save(user);

        return new ProfilePictureResponseDTO(message, url);
    }

    public Map<String, Object> updateOrSetPreferences(
        String token, PreferencesRequestDTO prefData
    ){
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findById(userId).get();
        
        if(user.getPreferences() == null){
            user.setPreferences(new Preferences());    
        }
        
        if(!prefData.visibility().equalsIgnoreCase(user.getPreferences().getVisibility()))
            user.getPreferences().setVisibility(prefData.visibility());
            
        if(prefData.genres() != null)
            user.getPreferences().setGenres(prefData.genres());

        User updatedUser = userRepo.save(user);

        Map<String, Object> data = new HashMap<>();
        data.put("message", "Preferences updated successfully.");
        data.put("preferences", updatedUser.getPreferences());
        return data;
    }

    public Map<String, Object> addGameToInventory(String token, String gameId) throws IllegalArgumentException {
        Map<String, Object> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findById(userId).get();

        if(!gameRepo.existsById(gameId))
            throw new IllegalArgumentException("A board game associated with ID: " + gameId + "does not exist.");

        if(user.getOwnedGames().contains(gameId))
            throw new IllegalStateException("Board game with id: " + gameId + " is already in user inventory.");

        user.getOwnedGames().add(gameId);
        user = userRepo.save(user);

        List<GameInventoryDTO> games = new ArrayList<>();
        int ownedGameCount = user.getOwnedGames().size();
        for(String id : user.getOwnedGames()){
            Boardgame game = gameRepo.findById(id).get();
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getImageURL(),
                game.getGenres()
            );
            games.add(dto);
        }

        result.put("message", "game successfully added to user inventory.");
        result.put("ownedGamesCount", ownedGameCount);
        result.put("games", games);

        return result;
    }

    public Map<String, Object> addGameToInventory(String token, OtherGameDTO gameInfo, MultipartFile gameImage) throws IOException {
        Map<String, Object> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findById(userId).get();

        // add the user provided game
        String gameTitle = AuthService.sanitize(gameInfo.title());
        String gameDesc = AuthService.sanitize(gameInfo.description());
        List<String> gameGenres = new ArrayList<>();
        for(String genre : gameInfo.genres()){
            String cleanGenre = AuthService.sanitize(genre);
            gameGenres.add(cleanGenre);
        }

        String fileName = bucket.uploadFile(gameImage, gameTitle);
        String imageUrl = bucket.getFileUrl(fileName);

        Boardgame newGame = new Boardgame(
            null,
            null,
            gameTitle,
            gameDesc,
            imageUrl,
            gameInfo.minPlayers(),
            gameInfo.maxPlayers(),
            gameInfo.minAge(),
            gameInfo.duration(),
            gameGenres
        );

        newGame = gameRepo.save(newGame);
        user.getOwnedGames().add(newGame.getId());
        user = userRepo.save(user);

        List<GameInventoryDTO> games = new ArrayList<>();
        int ownedGameCount = user.getOwnedGames().size();
        for(String id : user.getOwnedGames()){
            Boardgame game = gameRepo.findById(id).get();
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getImageURL(),
                game.getGenres()
            );
            games.add(dto);
        }

        result.put("message", "game successfully added to user inventory.");
        result.put("ownedGamesCount", ownedGameCount);
        result.put("games", games);

        return result;
    }

    public Map<String, Object> removeGameFromInventory(String token, String gameId) {
        Map<String, Object> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findById(userId).get();

        if(!user.getOwnedGames().remove(gameId))
            throw new IllegalArgumentException("Board game with id: " + gameId + " was not found in user inventory.");
        
        user = userRepo.save(user);
        
        List<GameInventoryDTO> games = new ArrayList<>();
        int ownedGameCount = user.getOwnedGames().size();
        for(String id : user.getOwnedGames()){
            Boardgame game = gameRepo.findById(id).get();
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(),
                game.getTitle(),
                game.getDescription(),
                game.getImageURL(),
                game.getGenres()
            );
            games.add(dto);
        }

        result.put("message", "successfully removed game from inventory.");
        result.put("ownedGamesCount", ownedGameCount);
        result.put("games", games);
   
        return result;
    }

    public FriendsListDTO getOwnFriendsList(String token) {
        String userId = jwtService.extractUserId(token).toString();
        List<Friendship> friendships = fsRepo.findByUserAndStatus(userId, FriendStatus.ACCEPTED);
        List<FriendDTO> friends = makeFriendsList(friendships, userId);

        return new FriendsListDTO(
            "User friends list successfully retrieved",
            friends,
            null
        );
    }

    public FriendsListDTO getUserFriendsList(String token, String userId) throws NoSuchElementException{
        // the person whose friend list is requested
        if(!userRepo.existsById(userId))
            throw new NoSuchElementException("User associated with id: " + userId + " does not exist.");

        String clientId = jwtService.extractUserId(token).toString(); // requester


        List<Friendship> friendships = fsRepo.findByUserAndStatus(userId, FriendStatus.ACCEPTED);
        List<Friendship> clientFriendships = fsRepo.findByUserAndStatus(clientId, FriendStatus.ACCEPTED);

        List<FriendDTO> friends = makeFriendsList(friendships, userId);
        List<FriendDTO> clientFriends = makeFriendsList(clientFriendships, clientId);

        List<FriendDTO> mutuals = new ArrayList<>();

        for(FriendDTO friend : friends){
            for(FriendDTO clientFriend : clientFriends){
                if(friend.id().equals(clientFriend.id())){
                    mutuals.add(friend);
                }
            }
        }

        return new FriendsListDTO(
            "User friends list successfully retrieved",
            friends,
            mutuals
        );
    }

    private List<FriendDTO> makeFriendsList(List<Friendship> friendships, String listOwner){
        List<FriendDTO> friends = new ArrayList<>();

        for(Friendship fs : friendships){
            String friendId = fs.getSender().equals(listOwner) ? fs.getReceiver() : fs.getSender();
            Optional<User> friendOp = userRepo.findById(friendId);
            
            if(friendOp.isEmpty()) // Just in case something happened with this user's account and their id isn't on our db
                continue;

            // make friend dto and add to friends array
            User friend = friendOp.get();
            FriendDTO dto = new FriendDTO(
                friend.getId(),
                friend.getUsername(),
                (friend.getFirstName() + " " + friend.getLastName()),
                friend.getProfilePicture()
            );
            friends.add(dto);
        }

        return friends;
    }

    public FriendRequestsDTO getFriendRequests(String token) {
        String userId = jwtService.extractUserId(token).toString();

        Friendship forExample = new Friendship();
        forExample.setReceiver(userId);
        forExample.setStatus(FriendStatus.REQUESTED);
        Example<Friendship> example = Example.of(forExample);
        List<Friendship> friendships = fsRepo.findAll(example);
        List<FriendRequestDTO> requests = new ArrayList<>();

        for(Friendship fs : friendships){
            String friendId = fs.getSender().equals(userId) ? fs.getReceiver() : fs.getSender();
            Optional<User> friendOp = userRepo.findById(friendId);
            
            if(friendOp.isEmpty()) // Just in case something happened with this user's account and their id isn't on our db
                continue;

            // make friend dto and add to friends array
            User friend = friendOp.get();
            FriendDTO friendDTO = new FriendDTO(
                friend.getId(),
                friend.getUsername(),
                (friend.getFirstName() + " " + friend.getLastName()),
                friend.getProfilePicture()
            );

            FriendRequestDTO request = new FriendRequestDTO(
                fs.getId(), 
                friendDTO
            );
            requests.add(request);
        }

        return new FriendRequestsDTO(
            "User friend request successfully retrieved",
            requests
        );
    }

    public FriendRequestResponseDTO sendFriendRequest(String token, String userId) throws IllegalAccessException, IllegalArgumentException, NoSuchElementException{
        String clientId = jwtService.extractUserId(token).toString();
        
        if(!userRepo.existsById(userId))
            throw new NoSuchElementException("User associated with id: " + userId + " does not exist.");

        if(clientId.equals(userId))
            throw new IllegalArgumentException("Users cannot send friend requests to themselves.");

        // check that these two don't have a friendship record already
        Optional<Friendship> existingfs = fsRepo.findFriendShipBetweenUsers(userId, clientId);

        if(existingfs.isPresent()){
            Friendship friendship = existingfs.get();
            if(friendship.getStatus() == FriendStatus.ACCEPTED)
                throw new IllegalAccessException("User is already friends with user of id: " + userId + ".");
            else if(friendship.getStatus() == FriendStatus.REQUESTED){
                String message;
                if(friendship.getSender().equals(clientId))
                    message = "User already sent a request to user associated with id: " + userId + ".";
                else
                    message = "User has a request from user associated with id: " + userId + ".";

                throw new IllegalAccessException(message);
            }
            else{
                friendship.setSender(clientId);
                friendship.setReceiver(userId);
                friendship.setStatus(FriendStatus.REQUESTED);
                fsRepo.save(friendship);
            }
        }
        else{
            Friendship friendship = new Friendship(clientId, userId);
            fsRepo.save(friendship);
        }
        
        // notify the receiver
        // Notification notification = null;
        // notificationService.send(userId, notification);

        return new FriendRequestResponseDTO(
            "Friend request successfully sent."
        );
    }

    public FriendRequestResponseDTO respondToFriendRequest(String token, String requestId, String status) throws NoSuchElementException, IllegalArgumentException, IllegalAccessException{
        // retrieve database objects
        Optional<Friendship> pre = fsRepo.findById(requestId);
        String clientId = jwtService.extractUserId(token).toString();
        User client = userRepo.findById(clientId).get();
        
        status = status.toUpperCase();
        
        // validation fr
        if(pre.isEmpty())
            throw new NoSuchElementException("Friend request with id: " + requestId + " does not exist.");

        if(!pre.get().getReceiver().equals(client.getId()))
            throw new IllegalAccessException("Friend request with id: " + requestId + " was not sent to the requesting user (client).");

        if(pre.get().getStatus() != FriendStatus.REQUESTED)
            throw new IllegalAccessException("Friend request with id: " + requestId + " already has a response.");

        FriendStatus newStatus = switch (status) {
            case "ACCEPT" -> FriendStatus.ACCEPTED;
            case "DECLINED" -> FriendStatus.DECLINED;
            default -> throw new IllegalArgumentException("Friend Request response status must be either \"accept\" or \"decline\".");

        };

        Friendship fs = pre.get();
        fs.setStatus(newStatus);
        fsRepo.save(fs);

        // notify sender that these users are friends now (on acceptance)


        return new FriendRequestResponseDTO(
            "Friend request response successfully recorded."
        );
        
    }

    public FriendRequestResponseDTO unfriendUser(String token, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unfriendUser'");
    }

}
