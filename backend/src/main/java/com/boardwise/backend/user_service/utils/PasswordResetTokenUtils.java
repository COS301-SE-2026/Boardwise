package com.boardwise.backend.user_service.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class PasswordResetTokenUtils {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    public static String generateToken(){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public static String hashToken(String token){
        String algo = "SHA-256";
        try{
            MessageDigest digest = MessageDigest.getInstance(algo);
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedBytes);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(""+ algo + " algorithm not found", e);
        }
    }
}
