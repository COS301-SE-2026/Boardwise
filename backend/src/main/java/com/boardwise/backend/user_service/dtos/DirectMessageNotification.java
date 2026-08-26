package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.NotificationType;

public record DirectMessageNotification(
    NotificationType type,
    String senderId,
    String message
) implements ChatNotification{

    public DirectMessageNotification(String senderId, String message){
        this(NotificationType.DIRECT_MESSAGE, senderId, message);
    }

    @Override
    public NotificationType getType(){
        return type;
    }
}
