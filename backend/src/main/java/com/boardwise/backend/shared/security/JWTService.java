package com.boardwise.backend.shared.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.boardwise.backend.user_service.models.TokenBlackList;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String key;
    private final TokenBlackListRepository tokenRepo;
    private final UserRepository userRepo;

    JWTService(TokenBlackListRepository tokenRepo, UserRepository userRepo) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
    }

    public String generateToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        int ttl = 90 * 60 * 1000;
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .and()
                .signWith(getKey())
                .compact();
                
    }

    public ObjectId extractUserId(String token) {
        String userId = extractClaim(token, Claims::getSubject);
        return new ObjectId(userId);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDeets) {
        String userId = extractUserId(token).toString();
        String username = userRepo.findById(userId).get().getUsername();
        
        return username.equals(userDeets.getUsername()) && 
        !isTokenExpired(token) && 
        !isTokenBlackListed(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiry = extractClaim(token, Claims::getExpiration);
        return expiry.before(new Date());
    }
    
    private boolean isTokenBlackListed(String token){
        String jti = extractClaim(token, Claims::getId);
        return tokenRepo.existsByJti(jti);
    }

    public void addToBlackList(String token){
        Claims claims = extractAllClaims(token);
        tokenRepo.save(
            new TokenBlackList(claims.getId(), 
            claims.getExpiration()
            .toInstant())
        );
    }

}
