package com.boardwise.backend.user_service.dtos;

public record FriendDTO(
    String id,
    String username,
    String fullname,
    String profilePicture
) {}
