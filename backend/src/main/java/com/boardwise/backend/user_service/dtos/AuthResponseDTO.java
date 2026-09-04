package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.dtos.response.SummaryUserResponseDto;

public record AuthResponseDTO(
    String message,
    SummaryUserResponseDto user,
    String accessToken
) {}
