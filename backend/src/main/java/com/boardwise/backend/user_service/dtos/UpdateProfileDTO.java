package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.models.Preferences;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileDTO(
    @Size(min = 3, message = "Username field must be at least 3 characters long")
    String username,
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must be at least 8 characters and contain at least one uppercase letter, number, and symbol"
    )
    String password,
    @Email(message = "Email format invalid")
    String emailAddress,
    @Size(min = 3, message = "Location field must be at least 3 characters long")
    String location,
    Preferences preferences
) {}
