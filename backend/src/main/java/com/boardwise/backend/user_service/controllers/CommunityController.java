package com.boardwise.backend.user_service.controllers;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.user_service.dtos.DeRsvpDTO;
import com.boardwise.backend.user_service.dtos.EventInfoDTO;
import com.boardwise.backend.user_service.dtos.EventInviteDTO;
import com.boardwise.backend.user_service.dtos.EventUpdateDTO;
import com.boardwise.backend.user_service.dtos.LiveEventStatusRequestDTO;
import com.boardwise.backend.user_service.dtos.LiveMessageRequestDTO;
import com.boardwise.backend.user_service.dtos.request.LiveEventRequestDTO;
import com.boardwise.backend.user_service.services.CommunityService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/sb/community")
@RequiredArgsConstructor 
public class CommunityController {

    private final CommunityService service;

    @GetMapping("/")
    public ResponseEntity<?> getEvents(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer page,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.getEvents(token, name, page);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getSpecificEvent(
        @PathVariable String eventId,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.getEvent(token, eventId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
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
        catch(NoSuchElementException e){
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
        @RequestPart(name = "EventInfo", required = false) 
        EventUpdateDTO newInfo,
        @RequestPart(name = "EventImage", required = false) 
        MultipartFile newImage,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.updateEvent(token, eventId, newInfo, newImage);
            return new ResponseEntity<>(res, HttpStatus.OK);
        } catch(IllegalAccessException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
        }catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        } catch(IOException e){
            res = new HashMap<>();
            res.put("message", "Failed to update event details. Something went wrong with event image.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch(Exception e){
            res = new HashMap<>();
            e.printStackTrace();
            res.put("message", "Failed to update event details. Something went wrong on our end.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(
        @PathVariable String eventId,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.cancelEvent(token, eventId);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
        catch(IllegalAccessException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.FORBIDDEN);
        }
        
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<?> rsvpToEvent(
        @PathVariable String eventId,
        HttpServletRequest req
    ) {
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.rsvp(token, eventId);
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/")
    public ResponseEntity<?> deRsvpFromEvent(
        @RequestBody DeRsvpDTO dto,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.deRsvp(token, dto);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(IllegalAccessException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } 
        catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/invite")
    public ResponseEntity<?> inviteToEvent(
        @RequestBody EventInviteDTO inviteInfo,
        HttpServletRequest req
    ) {
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.inviteToEvent(token, inviteInfo);        
            return new ResponseEntity<>(res, HttpStatus.CREATED);
        }
        catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
    }
    
    @PatchMapping("/invite/{eventId}")
    public ResponseEntity<?> respondToInvite(
        @RequestParam String status,
        @PathVariable String eventId,
        HttpServletRequest req
    ){
        String token = ProfileController.extractToken(req);
        Map<String, Object> res;
        try{
            res = service.respondToInvite(token, eventId, status);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(NoSuchElementException e){
            res = new HashMap<>();
            res.put("message", e.getMessage());
            return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/invite")
    public ResponseEntity<?> getInvitations(
        HttpServletRequest req
    ) {
        String token = ProfileController.extractToken(req);
        Map<String, Object> res = service.getUserInvitations(token);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/live-event")
    public ResponseEntity<?> createLiveEvent(HttpServletRequest req, @RequestBody LiveEventRequestDTO eventInfo){
        String token = ProfileController.extractToken(req);
        try {
            return new ResponseEntity<>(service.createLiveEvent(token, eventInfo), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", e.toString()));
        }
    }
    
    @GetMapping("/live-event/{eventId}")
    public ResponseEntity<?> getLiveEvent(@PathVariable String eventId){
        try{
            return ResponseEntity.ok(service.getLiveEvent(eventId));
        } catch(IllegalArgumentException | NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Live event not found"));
        }
    }

    @DeleteMapping("/live-event/{eventId}")
    public ResponseEntity<?> deleteLiveEvent(HttpServletRequest req, @PathVariable String eventId){
        String token = ProfileController.extractToken(req);
        try{
            return ResponseEntity.ok(service.deleteLiveEvent(token, eventId));
        } catch(ForbiddenException e){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch(IllegalArgumentException | NoSuchElementException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Live event not found"));
        }
    }

    @PostMapping("/live-event/{eventId}/join")
    public ResponseEntity<?> joinLiveEvent(HttpServletRequest req, @PathVariable String eventId){
        String token = ProfileController.extractToken(req);
        return ResponseEntity.ok(service.joinLiveEvent(token, eventId));
    }
    
    @PostMapping("/live-event/{eventId}/messages")
    public ResponseEntity<?> postLiveEventMessage(HttpServletRequest req, @PathVariable String eventId, @RequestBody LiveMessageRequestDTO body){
        String token = ProfileController.extractToken(req);
        return ResponseEntity.ok(service.postLiveEventMessage(token, eventId, body.content()));
    }

    @GetMapping("/live-event/{eventId}/messages")
    public ResponseEntity<?> getLiveEventMessages(HttpServletRequest req,@PathVariable String eventId, @RequestParam(required = false) Instant after){
        String token = ProfileController.extractToken(req);
        return ResponseEntity.ok(service.getLiveEventMessages(token, eventId, after));
    }

    @PutMapping("/live-event/{eventId}/status")
    public ResponseEntity<?> updateAttendeeStatus(HttpServletRequest req, @PathVariable String eventId,
                                                @RequestBody LiveEventStatusRequestDTO body){
        String token = ProfileController.extractToken(req);
        return ResponseEntity.ok(service.updateAttendeeStatus(token, eventId, body.status()));
    }
    
    @GetMapping("/live-events")
    public ResponseEntity<?> getLiveEvents(){
        return ResponseEntity.ok(service.getPublicLiveEvents());
    }
}

