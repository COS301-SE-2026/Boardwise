package com.boardwise.backend.user_service.dtos;

public record CommunityMessage(
    String id,    
    String communityId,
    String message
) {}
