package com.boardwise.backend.user_service.dtos;

import java.util.List;

import org.hibernate.validator.constraints.pl.REGON;

import com.boardwise.backend.user_service.models.Preferences;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record RegisterDTO(
    @NotBlank(message = "Username is required")
    @Size(min = 3, message = "Username field must be at least characters long")
    String username, 
    @NotBlank(message = "Email Address is required")
    @Email(message = "Email format invalid")
    String emailAddress,
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must be at least 8 characters and contain at least one uppercase letter, number, and symbol"
    )
    String password, 
    @NotBlank(message = "First Name is required")
    String firstName,
    @NotBlank(message = "Last Name is required") 
    String lastName, 
    String bio,
    Preferences preferences,
    List<String> ownedGames
) {}

