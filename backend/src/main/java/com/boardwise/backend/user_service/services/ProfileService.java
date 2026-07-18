package com.boardwise.backend.user_service.services;


import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.GameInventoryDTO;
import com.boardwise.backend.user_service.dtos.OtherGameDTO;
import com.boardwise.backend.user_service.dtos.PreferencesRequestDTO;
import com.boardwise.backend.user_service.dtos.ProfilePictureResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
import com.boardwise.backend.user_service.dtos.UpdateProfileDTO;
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

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public ProfileResponseDTO getOwnProfile(String token) {
        // get username from token
        ObjectId userId = jwtService.extractUserId(token);
        User extractedUser = userRepo.findById(userId.toString()).get();
        return getProfile(null, extractedUser);
    }

    public ProfileResponseDTO getProfile(String username, User own) {
        // get user data from db
        User user = own == null ? 
                    userRepo.findByUsername(username)
                                        .orElseThrow() :
                    own;
        
        
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
        List<Map<String, String>> communities = null;
        if(own != null){
            communities = new ArrayList<>();
            for(GroupMembership membership : gms){
                Map<String, String> community = new HashMap<>();
                Group group = groupRepo.findById(membership.getGroupId()).get();
                community.put("id", group.getId());
                community.put("name", group.getName());
                community.put("image", group.getImageUrl());

                communities.add(community);
            }
        }
        
        // get friend count
        int friendCount = (int) fsRepo.countByUserAIdOrUserBId(user.getId(), user.getId());

        DateTimeFormatter formatter = DateTimeFormatter
                                        .ofPattern("dd-MM-yyyy")
                                        .withZone(ZoneOffset.UTC);

        Preferences userPref = user.getPreferences();
        String fullName = user.getFirstName() + " " + user.getLastName();                 
        return new ProfileResponseDTO(
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

}
