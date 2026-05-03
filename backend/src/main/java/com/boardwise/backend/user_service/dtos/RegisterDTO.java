package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.models.Preferences;

public record RegisterDTO(String username, String emailAddress,
    String password, String firstName, String lastName, String bio,
    Preferences preferences
) {}
