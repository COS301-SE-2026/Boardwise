package com.boardwise.backend.user_service.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.user_service.dtos.GroupCreationDTO;
import com.boardwise.backend.user_service.dtos.GroupCreationResponseDTO;
import com.boardwise.backend.user_service.dtos.GroupDTO;
import com.boardwise.backend.user_service.dtos.GroupInfo;
import com.boardwise.backend.user_service.dtos.GroupMembershipResponseDTO;
import com.boardwise.backend.user_service.dtos.GroupUpdateRequestDTO;
import com.boardwise.backend.user_service.dtos.GroupUpdateResponseDTO;
import com.boardwise.backend.user_service.services.SocialService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService service;

    SocialController(SocialService service) {
        this.service = service;
    }

    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(
        HttpServletRequest req,
        @RequestPart("groupInfo") GroupCreationDTO group,
        @RequestPart("groupImage") MultipartFile image
    ){
        try{
            String token = ProfileController.extractToken(req);
            GroupCreationResponseDTO res = service.createGroup(token, group, image);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong during group creation");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<?> getAllGroups(
        HttpServletRequest req
    ) {
        try{
            String token = ProfileController.extractToken(req);
            List<?> groups = service.getAllGroups(token);
            Map<String, Object> res = new HashMap<>();
            res.put("groups", groups);
            return new ResponseEntity<>(res, HttpStatus.OK);

        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong when fetching all groups");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<?> getGroup(
        @PathVariable String groupId,
        HttpServletRequest req
    ) {
        try{
            String token = ProfileController.extractToken(req);
            GroupDTO res = service.getGroup(token, groupId);
            return new ResponseEntity<>(res, HttpStatus.OK);

        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong when fetching group.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/groups/{groupId}")
    public ResponseEntity<?> joinGroup(
        @PathVariable String groupId,
        HttpServletRequest req
    ){
        try{
            String token = ProfileController.extractToken(req);
            GroupMembershipResponseDTO res = service.addToGroup(token, groupId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(IllegalStateException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Could not add member, group of associated ID does not exist.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong while adding member to group");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> leaveGroup(
        @PathVariable String groupId,
        HttpServletRequest req
    ){
        try{
            String token = ProfileController.extractToken(req);
            GroupMembershipResponseDTO res = service.removeFromGroup(token, groupId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(IllegalStateException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.CONFLICT);
        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Could not remove member, group of associated ID does not exist.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong while removing member from group");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Editing group stuff
    @PatchMapping("/groups/{groupId}")
    public ResponseEntity<?> updateGroup(
        @PathVariable String groupId,
        @RequestBody GroupUpdateRequestDTO updateData,
        HttpServletRequest req
    ){
        try{
            String token = ProfileController.extractToken(req);
            GroupUpdateResponseDTO res = service.updateGroup(token, groupId, updateData);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Could not update, group of associated ID does not exist.");
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong while updating group information");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // search by group name
    @GetMapping("/groups/search/{groupName}")
    public ResponseEntity<?> getGroupByName(
        @PathVariable String groupName
    ){
        try{
            GroupInfo res = service.getGroup(groupName);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(Exception e){
            e.printStackTrace();
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong while retrieving group(s)");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
