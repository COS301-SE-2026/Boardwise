package com.boardwise.backend.user_service.dtos;

public record AuthResponseDTO(
    String message,
    String accessToken
) {
}
