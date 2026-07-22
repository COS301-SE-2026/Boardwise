package com.boardwise.backend.user_service.dtos;

import java.util.List;

import com.boardwise.backend.user_service.models.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;

public record GroupDTO(
    String id,
    String name,
    String imageUrl,
    String description,
    String owner,
    Visibility visibility,
    int memberCount,
    List<?> members,
    @JsonProperty("isMember")
    boolean isMember
) {}
