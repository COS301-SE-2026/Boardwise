package com.boardwise.backend.user_service.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordDto(
    @NotBlank(message = "Email Address is required")
    @Email(message = "Email format invalid")
    String emailAddress
) {}
