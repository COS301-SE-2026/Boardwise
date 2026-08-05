package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.models.Visibility;

public record GroupInfo(
    String id,
    String name,
    String imageUrl,
    String description,
    String owner,
    Visibility visibility,
    String category,
    int memberCount
) {}
