package com.boardwise.backend.user_service.dtos;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OtherGameDTO(
    @NotNull(message = "Title is a required field.")
    @Size(min = 1, message = "Title must have at least one character.")
    String title,
    @NotNull(message = "Description is a required field.")
    @Size(min = 1, message = "Description must have at least one character.")
    String description,
    @NotNull(message = "\"minPlayers\" is a required field.")
    @Min(value = 1L, message = "\"minPlayers\" field must be a value greater than or equal to 1.")
    int minPlayers,
    @NotNull(message = "\"maxPlayers\" is a required field.")
    int maxPlayers,
    @NotNull(message = "\"minAge\" is a required field.")
    int minAge,
    @NotNull(message = "\"duration\" is a required field.")
    @Min(value = 1L, message = "\"duration\" field must be a value greater than or equal to 1.")
    int duration,
    @NotNull(message = "Genres is a required field.")
    @Size(min = 1, message = "Game must belong to at least one genre.")
    List<String> genres
) {

    public OtherGameDTO{
        if(minPlayers >= maxPlayers)
            throw new IllegalArgumentException("Minimum players must be less than maximum players.");
    }
}
