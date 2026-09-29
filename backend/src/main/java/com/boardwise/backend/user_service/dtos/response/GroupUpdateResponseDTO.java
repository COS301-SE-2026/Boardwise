package com.boardwise.backend.user_service.dtos.response;

import java.util.Map;

public record GroupUpdateResponseDTO(
    String message,
    Map<String, Object> data
) {}
