package com.boardwise.backend.user_service.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PreferencesRequestDTO(
    @JsonProperty("isPrivate")
    boolean isPrivate,
    List<String> genres
) {}
