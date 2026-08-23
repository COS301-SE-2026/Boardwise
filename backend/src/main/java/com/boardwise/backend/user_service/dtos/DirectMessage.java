package com.boardwise.backend.user_service.dtos;

public record DirectMessage(
    String receiverId,
    String message
) {}
