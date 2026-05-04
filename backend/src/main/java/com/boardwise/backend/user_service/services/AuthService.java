package com.boardwise.backend.user_service.services;

import java.nio.file.attribute.UserPrincipalNotFoundException;

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
    private AuthenticationManager manager;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    // inserts user into database generates JWT
    public AuthResponseDTO register(RegisterDTO dto, MultipartFile pfp){
        // validate and sanitise data


        // insert into db
        

        // generate JWT and return it
        String token = jwt.generateToken(dto.username());
        return new AuthResponseDTO(token);
    }

    public AuthResponseDTO login(LoginDTO dto){
        // data validation and sanitisation (don't trust user)
        String username = userData.username();
        String password = userData.password();
        
        // validate user
        Authentication auth = manager
        .authenticate(new UsernamePasswordAuthenticationToken(username, password) );

        if(!auth.isAuthenticated())
            throw new IllegalArgumentException("Incorrect user credentials");

        // generate JWT and return it
        String token = jwt.generateToken(username);
        return new AuthResponseDTO(token);
    }

    public void logout(){}

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
