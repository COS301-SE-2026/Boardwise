package com.boardwise.backend.user_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boardwise.backend.user_service.dtos.AuthResponseDTO;
import com.boardwise.backend.user_service.dtos.LoginDTO;
import com.boardwise.backend.user_service.dtos.LogoutResponseDTO;
import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/sb/auth")
public class Authcontrollers {

    private final AuthService service;

    Authcontrollers(AuthService service) {
        this.service = service;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello bruv", HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterDTO userData
    ){
        AuthResponseDTO responseData = service.register(userData);
        return new ResponseEntity<>(responseData, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginDTO userData){
        AuthResponseDTO response = service.login(userData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(HttpServletRequest request){
        String token = request.getHeader("Authorization").split(" ")[1];
        LogoutResponseDTO response = service.logout(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
