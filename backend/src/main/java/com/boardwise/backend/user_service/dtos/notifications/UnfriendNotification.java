package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.enums.NotificationType;

public record UnfriendNotification(
    NotificationType type,
    String friendId
) implements NotificationDTO{

    public UnfriendNotification(String friendId){
        this(NotificationType.UNFRIEND, friendId);
    }

    @Override 
    public NotificationType getType(){
        return type;
    }
}
