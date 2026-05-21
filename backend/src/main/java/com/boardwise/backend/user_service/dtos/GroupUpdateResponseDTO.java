package com.boardwise.backend.user_service.dtos;

import java.util.Map;

public record GroupUpdateResponseDTO(
    String message,
    Map<String, Object> data
) {}
