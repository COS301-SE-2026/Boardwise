package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.models.Visibility;

import jakarta.validation.constraints.Size;

public record GroupUpdateRequestDTO(
    @Size(
        min = 3, 
        message = "Group name needs to be 3 characters or more"
    )
    String name,
    String description,
    Visibility visibility
) {}
