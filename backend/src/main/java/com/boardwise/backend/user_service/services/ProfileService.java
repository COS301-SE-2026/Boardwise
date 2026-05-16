package com.boardwise.backend.user_service.services;


import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private FriendShipRepository fsRepo;

    @Autowired
    private GroupMembershipRepository gmRepo;

    @Autowired
    private BoardGameRepository gameRepo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

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
        
        return new ProfileResponseDTO(
            user.getUsername(),
            user.getProfilePicture(),
            friendCount,
            groupCount,
            ownedGameCount,
            games,
            user.getPreferences(),
            formatter.format(user.getCreatedAt())
        );
    }

    public boolean deleteUser(String token) {
        String username = jwtService.extractUsername(token);
        int deletedUsers = (int) userRepo.deleteByUsername(username);
        
        return deletedUsers == 1;
    }

    public UpdateProfileDTO updateProfile(String token, UpdateProfileDTO profileUpdateData) {
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();

        String newUsername = AuthService.sanitize(profileUpdateData.username());
        String newEmail = AuthService.sanitize(profileUpdateData.emailAddress());
        String newPassword = (profileUpdateData.password() != null) ?
                                encoder.encode(profileUpdateData.password()) :
                                null;
        Map<String, String> toReturn = new HashMap<>();
        toReturn.put("username", null);
        toReturn.put("password", null);
        toReturn.put("email", null);

        if(newUsername != null){
            user.setUsername(newUsername);
            toReturn.put("username", newUsername);
        }
            
        if(newEmail != null){
            user.setEmailAddress(newEmail);
            toReturn.put("password", newPassword);
        }
            
        if(newPassword != null){
            user.setPassword(newPassword);
            toReturn.put("email", newEmail);
        }
        
        User updatedUser = userRepo.save(user);
        
        return new UpdateProfileDTO(
            updatedUser.getUsername(),
            null,
            updatedUser.getEmailAddress()
        );
    }

    public ProfilePictureResponseDTO changeProfilePicture(String token, MultipartFile pfp) {
        String url = "";
        String message = "";
        // String username = jwtService.extractUsername(token);

        // logic here

        return new ProfilePictureResponseDTO(message, url);
    }


}
