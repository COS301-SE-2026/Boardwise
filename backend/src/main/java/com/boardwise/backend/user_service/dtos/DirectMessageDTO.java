package com.boardwise.backend.user_service.dtos;

public record DirectMessageDTO(
    String id,
    String senderId,
    String recipientId,
    String message
) {
    public DirectMessageDTO(String senderId, DirectMessage message){
        this(message.id(), senderId, message.receiverId(), message.message());
    }
}
