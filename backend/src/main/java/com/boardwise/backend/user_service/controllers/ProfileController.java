package com.boardwise.backend.user_service.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.user_service.dtos.ProfileResponseDTO;
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
        String token = req.getHeader("Authorization").split(" ")[1];
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
            Map<String, Object> res = new HashMap<>();
            res.put("message", "Something went wrong on our end.");
            return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
