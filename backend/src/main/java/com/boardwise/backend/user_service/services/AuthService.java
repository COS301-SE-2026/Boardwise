package com.boardwise.backend.user_service.services;

import java.time.Instant;

import org.owasp.encoder.Encode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.EmailService;
import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.dtos.request.ForgotPasswordDto;
import com.boardwise.backend.user_service.dtos.request.ResetPasswordDto;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.boardwise.backend.user_service.utils.PasswordResetTokenUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepo;
    private final JWTService jwt;
    private final AuthenticationManager manager;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    private final EmailService emailService;

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
        newUser = userRepo.save(newUser);

        // generate JWT and return it
        String token = jwt.generateToken(newUser.getId());
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
        String token = jwt.generateToken(userRepo.findByUsername(username).get().getId());
        return new AuthResponseDTO("User logged in successfully", token);
    }

    public LogoutResponseDTO logout(String token){
        jwt.addToBlackList(token);
        return new LogoutResponseDTO("User successfully logged out");
    }

    public void forgotPassword(ForgotPasswordDto dto){
        String email = sanitize(dto.emailAddress());

        User user = userRepo.findByEmailAddress(email).orElse(null);
        if(user == null) return;

        String resetToken = PasswordResetTokenUtils.generateToken();
        String hashedToken = PasswordResetTokenUtils.hashToken(resetToken);

        user.setResetToken(hashedToken);
        int tokenExpiryMinutes = 15;
        user.setResetTokenExpiry(Instant.now().plusSeconds(60 * tokenExpiryMinutes));
        userRepo.save(user);

        String resetLink = frontendBaseUrl + "auth/reset?token="+ resetToken;
        emailService.sendPasswordResetEmail(user.getEmailAddress(), resetLink);
    }

    public void resetPassword(ResetPasswordDto dto){
        String token = dto.token();
        String password = passwordEncoder.encode(dto.newPassword());

        User user = userRepo.findByResetToken(PasswordResetTokenUtils.hashToken(token)).orElse(null);
        if(user == null){
            throw new IllegalArgumentException("Invalid password reset token.");
        }

        if(user.getResetTokenExpiry().isBefore(Instant.now())){
            throw new IllegalArgumentException("Password reset token expired");
        }

        user.setPassword(password);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepo.save(user);
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
