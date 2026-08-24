package com.boardwise.backend.user_service.dtos;

import org.springframework.boot.context.properties.bind.DefaultValue;

import com.boardwise.backend.user_service.enums.Visibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupCreationDTO(
    @NotNull
    @NotBlank
    @Size(
        min = 3, 
        message = "Group name must be at least 3 characters long, not composed of whitespaces"
    )
    String name,
    String description,
    @NotNull
    @NotBlank
    String category,
    @DefaultValue("PUBLIC")
    Visibility visibility
) {}
