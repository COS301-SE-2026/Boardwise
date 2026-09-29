package com.boardwise.backend.user_service.dtos;

public record GroupInviteInfo(
    String id,
    String name,
    String imageUrl,
    GroupMember owner
) {}
