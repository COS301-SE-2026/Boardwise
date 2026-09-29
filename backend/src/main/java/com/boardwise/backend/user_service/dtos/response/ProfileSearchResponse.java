package com.boardwise.backend.user_service.dtos.response;

import com.boardwise.backend.user_service.enums.FriendStatus;

public record ProfileSearchResponse(
    String id,
    String username,
    String fullName,
    String profilePicture,
    FriendStatus status
) {}
