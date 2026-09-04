package com.boardwise.backend.user_service.dtos.response;

public record SummaryUserResponseDto(
        String username,
        String emailAddress,
        String firstName,
        String lastName) {
}
