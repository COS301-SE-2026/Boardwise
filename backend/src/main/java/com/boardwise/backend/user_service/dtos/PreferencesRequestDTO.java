package com.boardwise.backend.user_service.dtos;

import java.util.List;

public record PreferencesRequestDTO(
    String visibility,
    List<String> genres
) {}
