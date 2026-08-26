package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.NotificationType;

public record CommunityMessageNotification(
    NotificationType type,
    String senderId,
    String message

) implements ChatNotification{

    public CommunityMessageNotification(String senderId, String message){
        this(NotificationType.COMMUNITY_CHAT, senderId, message);
    }

    @Override
    public NotificationType getType(){
        return type;
    }

}   
