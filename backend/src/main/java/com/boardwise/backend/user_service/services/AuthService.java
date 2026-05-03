package com.boardwise.backend.user_service.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.user_service.dtos.RegisterDTO;
import com.boardwise.backend.user_service.repos.UserRepository;

@Service
public class AuthService {

    private UserRepository userRepo;

    // inserts user into database generates JWT
    public String register(RegisterDTO userData, MultipartFile pfp){
        // validate and sanitise

        // insert into db

        // generate JWT and return it
        return new String();
    }
}
