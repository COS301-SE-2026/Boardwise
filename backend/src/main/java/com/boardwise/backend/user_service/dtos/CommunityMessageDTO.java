package com.boardwise.backend.user_service.dtos;

public record CommunityMessageDTO(
    String id,
    String senderId,
    String communityId,
    String message
) {
    public CommunityMessageDTO(String senderId, CommunityMessage message){
        this(message.id(), senderId, message.communityId(), message.message());
    }
}
