package com.boardwise.backend.user_service.dtos;

public record GroupInfo(
    String id,
    String name,
    String imageUrl,
    String description,
    String owner,
    String visibility,
    String category,
    int memberCount
) {}
