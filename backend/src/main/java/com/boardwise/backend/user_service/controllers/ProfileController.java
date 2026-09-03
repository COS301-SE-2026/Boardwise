package com.boardwise.backend.user_service.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.boardwise.backend.shared.dtos.OtherGameDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestResponseDTO;
import com.boardwise.backend.user_service.dtos.FriendRequestsDTO;
import com.boardwise.backend.user_service.dtos.FriendsListDTO;
import com.boardwise.backend.user_service.dtos.NotificationsDTO;
import com.boardwise.backend.user_service.dtos.PreferencesRequestDTO;
import com.boardwise.backend.user_service.dtos.ProfilePictureResponseDTO;
import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
import com.boardwise.backend.user_service.dtos.UpdateProfileDTO;
import com.boardwise.backend.user_service.dtos.request.BoardgameCollectionBulkAddDto;
import com.boardwise.backend.user_service.dtos.response.BulkAddResponseDTO;
import com.boardwise.backend.user_service.services.ProfileService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class ProfileController {

    private final ProfileService service;

    ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getOtherUserProfile(@PathVariable String userId){
        try{
            ProfileResponseDTO res = service.getProfile(userId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong on our end.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/")
    public ResponseEntity<?> getOwnProfile(
        HttpServletRequest req,
        @RequestParam(name = "search", required = false) String query
    ){
        String token = extractToken(req);
        try{
            var res = (query == null || query.isBlank()) ? service.getOwnProfile(token) : service.searchForUsers(query, token);
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
        service.deleteUser(token);
        res.put("message", "Account deleted successfully.");
        return new ResponseEntity<>(res, HttpStatus.OK);
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
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
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
 
    // add game (when the game comes from our list)
    @PostMapping("/gameInventory/{gameId}")
    public ResponseEntity<?> addGameToInventory(
        @PathVariable String gameId,
        HttpServletRequest req
    ){
        String token = extractToken(req);
        Map<String, Object> res;
        try{
            res = service.addGameToInventory(token, gameId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch(IllegalArgumentException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
    }

    // add game (when game is not in our database, other is selected)
    @PostMapping("/gameInventory")
    public ResponseEntity<?> addGameToInventory(
        @RequestPart("gameInfo") OtherGameDTO gameInfo,
        @RequestPart("gameImage") MultipartFile gameImage,
        HttpServletRequest req
    ){
        String token = extractToken(req);
        Map<String, Object> res;
        try{
            res = service.addGameToInventory(token, gameInfo, gameImage);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        } catch(IOException e){
            res = new HashMap<>();
            res.put("message", "Something went wrong while adding game to inventory.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // remove game
    @DeleteMapping("/gameInventory/{gameId}")
    public ResponseEntity<?> removeGameFromInventory(
        @PathVariable String gameId,
        HttpServletRequest req
    ){
        String token = extractToken(req);
        Map<String, Object> res;
        try{
            res = service.removeGameFromInventory(token, gameId);
            return new ResponseEntity<>(res, HttpStatus.OK);
            
        } catch(IllegalArgumentException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/gameInventory/bulk")
    public ResponseEntity<?> addGamesToInventory(
        @RequestBody BoardgameCollectionBulkAddDto dto,
        HttpServletRequest req
    ){
        String token = extractToken(req);
        try {
            BulkAddResponseDTO res = service.bulkAddGameToInventory(token, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong while adding game to inventory.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // own friends list
    @GetMapping("/friends")
    public ResponseEntity<?> getOwnFriendsList(
        HttpServletRequest req
    ){
        String token = extractToken(req);
        FriendsListDTO friends = service.getOwnFriendsList(token);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    // get another user's friends list
    @GetMapping("/{userId}/friends")
    public ResponseEntity<?> getOtherUserFriendsList(
        @PathVariable String userId,
        HttpServletRequest req
    ) {
        try{
            String token = extractToken(req);
            FriendsListDTO friends = service.getUserFriendsList(token, userId);
            return new ResponseEntity<>(friends, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
    }
    
    // get friends requests
    @GetMapping("/friendRequests")
    public ResponseEntity<?> getFriendRequests(
        HttpServletRequest req
    ) {
        String token = extractToken(req);
        FriendRequestsDTO res = service.getFriendRequests(token);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
    // send friend request to a user (userId)
    @PostMapping("/{userId}/friendRequests")
    public ResponseEntity<?> sendFriendRequest(
        @PathVariable String userId,
        HttpServletRequest req
    ) {
        try{
            String token = extractToken(req);
            FriendRequestResponseDTO res = service.sendFriendRequest(token, userId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(IllegalAccessException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // respond to friend request (requestId)
    @PatchMapping("/friendRequests/{requestId}")
    public ResponseEntity<?> respondToFriendRequest(
        @PathVariable String requestId,
        @RequestParam String status,
        HttpServletRequest req
    ) {
        try{
            String token = extractToken(req);
            FriendRequestResponseDTO res = service.respondToFriendRequest(token, requestId, status);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(IllegalAccessException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    // unfriend a user
    @DeleteMapping("/friends/{userId}")
    public ResponseEntity<?> unfriendUser(
        @PathVariable String userId,
        HttpServletRequest req
    ) {
        try{
            String token = extractToken(req);
            FriendRequestResponseDTO res = service.unfriendUser(token, userId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(IllegalAccessException e){
            Map<String, String> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getMissedNotifications(
        HttpServletRequest req
    ) {
        String token = extractToken(req);
        NotificationsDTO res = service.getMissedNotifications(token);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    

    public static String extractToken(HttpServletRequest req){
        String header = req.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer "))
            throw new IllegalArgumentException("Missing or invalid JWT token");

        return header.substring(7);
    }

}
