package com.boardwise.backend.user_service.dtos.request;

import java.util.List;
import com.boardwise.backend.user_service.enums.Visibility;

public record PreferencesRequestDTO(
    Visibility visibility,
    List<String> genres
) {}
