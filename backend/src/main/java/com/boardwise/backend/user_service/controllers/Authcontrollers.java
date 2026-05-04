package com.boardwise.backend.user_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.services.AuthService;


@RestController
@RequestMapping("/api/auth/")
public class Authcontrollers {

    @Autowired
    private AuthService service;

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello bruv", HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Validated @RequestPart("userData") RegisterDTO userData,
        @RequestPart("profilePic") MultipartFile pfp
    ){
        AuthResponseDTO responseData = service.register(userData, pfp);
        return new ResponseEntity<>(responseData, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Validated @RequestBody LoginDTO userData){
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<AuthResponseDTO> logout(){
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
