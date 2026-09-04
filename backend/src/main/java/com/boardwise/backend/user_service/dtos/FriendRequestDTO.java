package com.boardwise.backend.user_service.dtos;

public record FriendRequestDTO(
    String id, // id of the request
    FriendDTO sender // the person who sent the request
) {}
