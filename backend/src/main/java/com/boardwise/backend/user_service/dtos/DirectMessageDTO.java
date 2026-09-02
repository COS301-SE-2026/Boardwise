package com.boardwise.backend.user_service.dtos;

import java.time.Instant;

public record DirectMessageDTO(
    String id,
    String senderId,
    String receiverId,
    String message,
    Instant sentAt
) {
    public DirectMessageDTO(String senderId, DirectMessage message, Instant sentAt){
        this(message.id(), senderId, message.receiverId(), message.message(), sentAt);
    }
}
