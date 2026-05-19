package com.boardwise.backend.user_service.dtos;

public record GroupInfo(
    String groupId,
    String name,
    String description,
    String owner,
    String visibility,
    int memberCount
) {}
