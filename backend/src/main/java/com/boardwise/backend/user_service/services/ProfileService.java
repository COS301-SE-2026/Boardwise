package com.boardwise.backend.user_service.services;


import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.PreferencesRequestDTO;
import com.boardwise.backend.user_service.dtos.ProfilePictureResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
import com.boardwise.backend.user_service.dtos.UpdateProfileDTO;
import com.boardwise.backend.user_service.models.*;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.FriendShipRepository;
import com.boardwise.backend.user_service.repos.GroupMembershipRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

@Service
public class ProfileService {

    private final UserRepository userRepo;
    private final JWTService jwtService;
    private final FriendShipRepository fsRepo;
    private final GroupMembershipRepository gmRepo;
    private final BoardGameRepository gameRepo;
    private final R2StorageService bucket;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    ProfileService(
        UserRepository userRepo, 
        JWTService jwtService,
        FriendShipRepository friendShipRepository,
        GroupMembershipRepository groupMembershipRepository,
        BoardGameRepository boardGameRepository,
        R2StorageService r2StorageService
    ){
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.fsRepo = friendShipRepository;
        this.gmRepo = groupMembershipRepository;
        this.gameRepo = boardGameRepository;
        this.bucket = r2StorageService;
    }

    public ProfileResponseDTO getOwnProfile(String token) {
        // get username from token
        String extractedUsername = jwtService.extractUsername(token);
        return getProfile(extractedUsername);
    }

    public ProfileResponseDTO getProfile(String username) {
        // get user data from db
        User user = userRepo.findByUsername(username)
                                        .orElseThrow();
        
        
        // get the games from stored ids                                
        List<Boardgame> games = new ArrayList<>();
        int ownedGameCount = user.getOwnedGames().size();
        for(String gameId : user.getOwnedGames()){
            Boardgame game = gameRepo.findById(gameId).get();
            games.add(game);
        }

        // get group count
        GroupMembership gm = new GroupMembership();
        gm.setUserId(user.getId());
        int groupCount = (int) gmRepo.count(Example.of(gm));
        
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
            user.getProfilePicture(),
            friendCount,
            groupCount,
            ownedGameCount,
            games,
            userPref,
            formatter.format(user.getCreatedAt())
        );
    }

    public boolean deleteUser(String token) {
        String username = jwtService.extractUsername(token);
        // add removal of associated data

        // end associated data removal
        int deletedUsers = (int) userRepo.deleteByUsername(username);
        
        return deletedUsers == 1;
    }

    public Map<String, Object> updateProfile(String token, UpdateProfileDTO profileUpdateData) {
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();

        String newUsername = AuthService.sanitize(profileUpdateData.username());
        String newEmail = AuthService.sanitize(profileUpdateData.emailAddress());
        String newPassword = (profileUpdateData.password() != null) ?
                                encoder.encode(profileUpdateData.password()) :
                                null;
        

        Map<String, Object> toReturn = new HashMap<>();
      
        if(newUsername != null){
            user.setUsername(newUsername);
            toReturn.put("username", newUsername);
        }
            
        if(newEmail != null){
            user.setEmailAddress(newEmail);
            toReturn.put("email", newEmail);
        }
            
        if(newPassword != null){
            user.setPassword(newPassword);
            toReturn.put("password", newPassword);
        }

        userRepo.save(user);
        
        return toReturn;
    }

    public ProfilePictureResponseDTO changeProfilePicture(String token, MultipartFile pfp) throws IOException {
        String url = "";
        String message = "";
        String username = jwtService.extractUsername(token);

        // logic here
        String fileName = bucket.uploadFile(pfp, username);
        url = bucket.getFileUrl(fileName);
        message = "Profile picture successfully update";
         
        User user = userRepo.findByUsername(username).get();
        user.setProfilePicture(url);
        userRepo.save(user);

        return new ProfilePictureResponseDTO(message, url);
    }

    public Map<String, Object> updateOrSetPreferences(
        String token, PreferencesRequestDTO prefData
    ){
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();
        
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
}
