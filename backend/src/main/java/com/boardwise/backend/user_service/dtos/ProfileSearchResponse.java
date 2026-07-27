package com.boardwise.backend.user_service.dtos;

public record ProfileSearchResponse(
    String id,
    String username,
    String fullName,
    String profilePicture
) {}
