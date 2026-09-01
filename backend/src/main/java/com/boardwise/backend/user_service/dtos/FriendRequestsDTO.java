package com.boardwise.backend.user_service.dtos;

import java.util.List;

public record FriendRequestsDTO(
    String message,
    List<FriendRequestDTO> requests
) {}
