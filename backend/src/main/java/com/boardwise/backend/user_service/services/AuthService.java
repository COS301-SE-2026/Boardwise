package com.boardwise.backend.user_service.services;

import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;

@Service
public class AuthService {
    
    @Autowired  
    private UserRepository userRepo;

    @Autowired
    private JWTService jwt;

    @Autowired
    @Lazy
    private AuthenticationManager manager;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // inserts user into database generates JWT
    public AuthResponseDTO register(RegisterDTO dto){
        
        // sanitise data
        String username = sanitize(dto.username());
        String email = sanitize(dto.emailAddress());
        String firstName = sanitize(dto.firstName());
        String lastName = sanitize(dto.lastName());
        String password = passwordEncoder.encode(dto.password());

        // insert into db
        User newUser = new User(username, firstName, lastName, email, password);
        userRepo.save(newUser);

        // generate JWT and return it
        String token = jwt.generateToken(username, newUser.getId());
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
        String token = jwt.generateToken(username, userRepo.findByUsername(username).get().getId());
        return new AuthResponseDTO("User logged in successfully", token);
    }

    public LogoutResponseDTO logout(String token){
        jwt.addToBlackList(token);
        return new LogoutResponseDTO("User successfully logged out");
    }

    public static String sanitize(String input) {
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
