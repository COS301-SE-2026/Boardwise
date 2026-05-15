package com.boardwise.backend.user_service.services;


import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
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
}
