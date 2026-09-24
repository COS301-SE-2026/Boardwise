package com.boardwise.backend.user_service.dtos.response;

public record PresenceResponseDTO(
    String message,
    boolean isOnline
) {}
