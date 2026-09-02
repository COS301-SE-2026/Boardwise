package com.boardwise.backend.user_service.dtos;

import java.time.Instant;

public record ConversationDTO(
    String id,
    String userId,
    String username,
    String profilePicture,
    String lastMessage,
    Instant lastMessageAt
) {}
