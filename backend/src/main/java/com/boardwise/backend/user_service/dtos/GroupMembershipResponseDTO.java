package com.boardwise.backend.user_service.dtos;

import java.util.Map;

public record GroupMembershipResponseDTO(
    String message,
    Map<String, Object> data
) {}
