package com.boardwise.backend.user_service.dtos.response;


public record AuthResponseDTO(
    String message,
    SummaryUserResponseDto user,
    String accessToken
) {}
