package com.boardwise.backend.user_service.services;

import java.util.ArrayList;
import java.util.List;
import com.boardwise.backend.user_service.models.Preferences;
import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

@Service
public class AuthService {
    
    @Autowired  
    private UserRepository userRepo;

    @Autowired  
    private BoardGameRepository gameRepo;

    @Autowired
    private JWTService jwt;

    @Autowired
    private AuthenticationManager manager;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // inserts user into database generates JWT
    public AuthResponseDTO register(RegisterDTO dto, MultipartFile pfp){
        // sanitise data
        String username = sanitize(dto.username());
        String email = sanitize(dto.emailAddress());
        String firstName = sanitize(dto.firstName());
        String lastName = sanitize(dto.lastName());
        String bio = sanitize(dto.bio());
        Preferences preferences = dto.preferences(); 
        if(preferences != null){
            List<String> sanPreferences = new ArrayList<>();
            for (String pref : preferences.getGenres()) {
                sanPreferences.add(sanitize(pref));
            }
            preferences.setGenres(sanPreferences);
        }
        List<String> ownedgames = new ArrayList<>();
        if(dto.ownedGames() != null){
            for (String gameName : dto.ownedGames()) {
                gameRepo.findByTitle(gameName)
                    .ifPresent(game -> ownedgames.add(game.getId()));
                // silently skip if game not found in DB
            }
        }
        String password = passwordEncoder.encode(dto.password());

        // insert into db
        User newUser = new User(username, firstName, lastName, email, password, bio, preferences, ownedgames);
        userRepo.save(newUser);

        // generate JWT and return it
        String token = jwt.generateToken(username);
        return new AuthResponseDTO("User successfully register", token);
    }

    public AuthResponseDTO login(LoginDTO dto){
        // data validation and sanitisation (don't trust user)
        String username = sanitize(dto.username());
        
        // validate user
        Authentication auth = manager
        .authenticate(new UsernamePasswordAuthenticationToken(username, dto.password()) );

        if(!auth.isAuthenticated())
            throw new IllegalArgumentException("Incorrect user credentials");

        // generate JWT and return it
        String token = jwt.generateToken(username);
        return new AuthResponseDTO("User logged in successfully", token);
    }

    public LogoutResponseDTO logout(String token){
        jwt.addToBlackList(token);
        return new LogoutResponseDTO("User successfully logged out");
    }

    private String sanitize(String input) {
        if (input == null) return null;
        
        // trim whitespace
        String sanitized = input.trim();
        
        // strip HTML tags
        sanitized = sanitized.replaceAll("<[^>]*>", "");
        
        // encode any remaining special characters
        sanitized = Encode.forHtml(sanitized);
        
        // block NoSQL injection operators
        if (sanitized.contains("$") || sanitized.contains("{")) {
            throw new IllegalArgumentException("Invalid characters in input");
        }
        
        return sanitized;
    }
}
