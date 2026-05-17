package com.boardwise.backend.user_service.dtos;

import java.util.List;

public record PreferencesRequestDTO(
    boolean isPrivate,
    List<String> genres
) {}
