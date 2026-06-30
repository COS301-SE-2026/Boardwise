package com.boardwise.backend.user_service.dtos;

import java.util.List;
import com.boardwise.backend.user_service.models.Preferences;

public record ProfileResponseDTO(
    String fullName,
    String username,
    String profilePicture,
    int friendCount,
    int groupCount,
    int ownedGameCount,
    List<GameInventoryDTO> games,
    Preferences preferences,
    String createdAt
) {}
