package com.boardwise.backend.user_service.dtos.request;

public record ResetPasswordDto(
    String token,
    String newPassword
){}
