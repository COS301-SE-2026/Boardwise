package com.boardwise.backend.user_service.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.boardwise.backend.user_service.dtos.PreferencesRequestDTO;
import com.boardwise.backend.user_service.dtos.ProfilePictureResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
import com.boardwise.backend.user_service.dtos.UpdateProfileDTO;
import com.boardwise.backend.user_service.services.ProfileService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users/")
public class ProfileController {

    @Autowired
    private ProfileService service;

    @GetMapping("/{username}")
    public ResponseEntity<?> getProfile(@PathVariable String username){
        try{
            ProfileResponseDTO res = service.getProfile(username);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "User with that username does not exist.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong on our end.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getOwnProfile(HttpServletRequest req){
        String token = extractToken(req);
        try{
            ProfileResponseDTO res = service.getOwnProfile(token);
            
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "User of associated token does not exist.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong on our end.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteProfile(HttpServletRequest req){
        String token = extractToken(req);
        Map<String, Object> res = new HashMap<>();
        if(service.deleteUser(token)){ 
            res.put("message", "Account deleted successfully.");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        res.put("message", "Failed to delete account. Something went wrong on our side.");
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/")
    public ResponseEntity<?> updateProfile(
        @RequestBody UpdateProfileDTO profileUpdateData,
        HttpServletRequest req
    ){
        try{
            String token = extractToken(req);
            Map<String, Object> res = service.updateProfile(token, profileUpdateData);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(Exception e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong during profile update.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/profilePicture")
    public ResponseEntity<?> updateProfilePicture(
        @RequestPart("profilePicture") MultipartFile pfp,
        HttpServletRequest req
    ){
        try{
            System.out.println("Is multipart: " + (req instanceof MultipartHttpServletRequest));
            System.out.println("Parts: " + req.getParts());
            String token = extractToken(req);
            ProfilePictureResponseDTO res = service.changeProfilePicture(token, pfp);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(IOException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong during profile picture update.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/preferences")
    public ResponseEntity<?> updateOrSetPreferences(
        @RequestBody PreferencesRequestDTO prefData,
        HttpServletRequest req
    ) {
        try{
            String token = extractToken(req);
            Map<String, Object> res = service.updateOrSetPreferences(token, prefData);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(Exception e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong during preferences update.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    public static String extractToken(HttpServletRequest req){
        String header = req.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid JWT token");

        return header.substring(7);
    }

}
