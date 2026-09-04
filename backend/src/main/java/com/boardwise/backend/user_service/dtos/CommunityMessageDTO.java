package com.boardwise.backend.user_service.dtos;

import java.time.Instant;

public record CommunityMessageDTO(
    String id,
    String senderId,
    String communityId,
    String message,
    Instant sentAt
) {
    public CommunityMessageDTO(String senderId, CommunityMessage message, Instant sentAt){
        this(message.id(), senderId, message.communityId(), message.message(), sentAt);
    }
}
