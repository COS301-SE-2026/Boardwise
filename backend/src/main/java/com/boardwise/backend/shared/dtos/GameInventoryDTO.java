package com.boardwise.backend.shared.dtos;

import java.util.List;

public record GameInventoryDTO(
    String id,
    String title,
    String description,
    String imageUrl,
    List<String> genres
) {}
