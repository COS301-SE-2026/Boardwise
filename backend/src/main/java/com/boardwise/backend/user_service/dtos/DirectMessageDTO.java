package com.boardwise.backend.user_service.dtos;

public record DirectMessageDTO(
    String senderId,
    String recipientId,
    String message
) {
    public DirectMessageDTO(String senderId, DirectMessage message){
        this(senderId, message.receiverId(), message.message());
    }
}
