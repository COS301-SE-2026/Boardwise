package com.boardwise.backend.shared.security;

import java.util.function.Function;

import javax.crypto.SecretKey;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public ObjectId extractUserId(String token) {
        String userId = extractClaim(token, claims -> claims.get("userId", String.class));
        return new ObjectId(userId);
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver){
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
