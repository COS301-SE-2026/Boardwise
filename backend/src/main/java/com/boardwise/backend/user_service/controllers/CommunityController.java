package com.boardwise.backend.user_service.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.user_service.dtos.DeRsvpDTO;
import com.boardwise.backend.user_service.dtos.EventInfoDTO;
import com.boardwise.backend.user_service.dtos.EventInviteDTO;
import com.boardwise.backend.user_service.dtos.EventUpdateDTO;
import com.boardwise.backend.user_service.services.CommunityService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityService service;

    CommunityController(CommunityService service){
        this.service = service;
    }

    @GetMapping("/")
    public ResponseEntity<?> getEvents(){
        Map<String, Object> res = service.getEvents();
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> createEvent(
        @RequestPart("EventInfo") EventInfoDTO eventInfo,
        @RequestPart("EventImage") MultipartFile eventImg,
        HttpServletRequest req
    ) {
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.createEvent(token, eventInfo, eventImg);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        catch(IllegalArgumentException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(Exception e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
        @PathVariable String eventId,
        @RequestPart("EventInfo") EventUpdateDTO newInfo,
        @RequestPart("EventImage") MultipartFile newImage,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.updateEvent(token, newInfo, newImage);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(
        @PathVariable String eventId,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.deleteEvent(token, eventId);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<?> rsvpToEvent(
        @PathVariable String eventId,
        HttpServletRequest req
    ) {
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.rsvp(token, eventId);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deRsvpFromEvent(
        @RequestBody DeRsvpDTO dto,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.deRsvp(token, dto);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteToEvent(
        @RequestBody EventInviteDTO inviteInfo,
        HttpServletRequest req
    ) {
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.inviteToEvent(token, inviteInfo);        
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    
    
    
}
