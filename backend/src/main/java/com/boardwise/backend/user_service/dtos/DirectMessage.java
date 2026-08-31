package com.boardwise.backend.user_service.dtos;

public record DirectMessage(
    String id,
    String receiverId,
    String message
) {}
