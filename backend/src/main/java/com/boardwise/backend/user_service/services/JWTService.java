package com.boardwise.backend.user_service.services;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.boardwise.backend.user_service.models.TokenBlackList;
import com.boardwise.backend.user_service.repos.TokenBlackListRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTService {

    private String key = "";

    @Autowired
    private TokenBlackListRepository tokenRepo;

    public JWTService(){
        try {
            KeyGenerator generator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secret = generator.generateKey();
            key = Base64.getEncoder().encodeToString(secret.getEncoded());

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        int ttl = 30 * 60 * 1000;
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .and()
                .signWith(getKey())
                .compact();
                
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(key);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
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
        String username = extractUsername(token);
        return username.equals(userDeets.getUsername()) && !isTokenExpired(token) 
        && !isTokenBlackListed(token);
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
