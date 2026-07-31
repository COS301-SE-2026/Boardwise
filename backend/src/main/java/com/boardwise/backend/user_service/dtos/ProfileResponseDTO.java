package com.boardwise.backend.user_service.dtos;

import java.util.List;
import java.util.Map;
import com.boardwise.backend.user_service.models.Preferences;

public record ProfileResponseDTO(
    String id,
    String fullName,
    String username,
    String location,
    String profilePicture,
    int friendCount,
    int groupCount,
    int ownedGameCount,
    List<GameInventoryDTO> games,
    List<Map<String, String>> communities,
    Preferences preferences,
    String createdAt
) {}
