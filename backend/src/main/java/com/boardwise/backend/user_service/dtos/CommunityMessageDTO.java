package com.boardwise.backend.user_service.dtos;

public record CommunityMessageDTO(
    String senderId,
    String communityId,
    String message
) {
    public CommunityMessageDTO(String senderId, CommunityMessage message){
        this(senderId, message.communityId(), message.message());
    }
}
