package com.boardwise.backend.user_service.dtos;

import java.util.List;

public record MessagesDTO(
    String message,
    List<?> data
) {}
