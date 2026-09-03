package com.boardwise.backend.user_service.dtos.response;

import java.util.List;

import com.boardwise.backend.shared.dtos.GameInventoryDTO;

public record BulkAddResponseDTO(
    String message,
    int ownedGamesCount,
    List<GameInventoryDTO> games
) {}
