package com.boardwise.backend.user_service.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginDTO(
    @NotBlank(message = "Username is required")
    @Size(min = 3, message = "Username field must be at least characters long")
    String username, 
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$",
        message = "Password must be at least 8 characters and contain at least one uppercase letter, number, and symbol"
    )
    String password
) {
}
