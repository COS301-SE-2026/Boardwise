package com.boardwise.backend.user_service.dtos;

public record ErrorMessage(
    String type,
    String message
) {}
