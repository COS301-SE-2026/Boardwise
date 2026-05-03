package com.boardwise.backend.user_service.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/")
public class Authcontrollers {

    

    @PostMapping("/register")
    public void register(){

    }

    @PostMapping("/login")
    public void login(){

    }

    @DeleteMapping("/logout")
    public void logout(){

    }
}
