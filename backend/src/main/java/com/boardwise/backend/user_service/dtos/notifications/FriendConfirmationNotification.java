package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.dtos.FriendDTO;
import com.boardwise.backend.user_service.enums.NotificationType;

public record FriendConfirmationNotification(
    NotificationType type,
    FriendDTO friend
) implements NotificationDTO{

    public FriendConfirmationNotification(FriendDTO friend){
        this(NotificationType.FRIEND_CONFIRMATION, friend);
    }

    @Override
    public NotificationType getType(){
        return type;
    }
}
