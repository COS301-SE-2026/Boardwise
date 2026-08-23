package com.boardwise.backend.user_service.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.models.UserDetailImpl;
import com.boardwise.backend.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).orElseThrow(
            () -> new UsernameNotFoundException(username + "does not exist")
        );

        return new UserDetailImpl(user);
    }

    public UserDetails loadUserByUserId(String userId) throws Exception {
        User user = userRepo.findById(userId).orElseThrow(
            () -> new Exception("No user is associated with ID: " + userId)
        );

        return new UserDetailImpl(user);
    }

}
