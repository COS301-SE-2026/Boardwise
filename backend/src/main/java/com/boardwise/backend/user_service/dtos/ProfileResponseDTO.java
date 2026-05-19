package com.boardwise.backend.user_service.dtos;

import java.util.List;

import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.Preferences;

public record ProfileResponseDTO(
    String username,
    String profilePicture,
    int friendCount,
    int groupCount,
    int ownedGameCount,
    List<Boardgame> games,
    Preferences preferences,
    String createdAt
) {}
