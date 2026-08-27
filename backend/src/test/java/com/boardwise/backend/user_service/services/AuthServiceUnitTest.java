package com.boardwise.backend.user_service.services;

import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.repository.UserRepository;

@DisplayName("Authentication Service Unit Tests")
public class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepo;
    
    @Mock
    private JWTService jwt;

    @Mock
    private AuthenticationManager manager;


}
