package com.boardwise.backend.user_service.dtos.response;

import com.boardwise.backend.user_service.dtos.GroupInfo;

public record GroupCreationResponseDTO(
    String message,
    GroupInfo group
) {}
