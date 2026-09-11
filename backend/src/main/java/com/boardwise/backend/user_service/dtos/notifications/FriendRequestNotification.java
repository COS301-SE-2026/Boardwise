package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.dtos.FriendRequestDTO;
import com.boardwise.backend.user_service.enums.NotificationType;

public record FriendRequestNotification(
    NotificationType type,
    FriendRequestDTO request
) implements NotificationDTO {
    public FriendRequestNotification(FriendRequestDTO request){
        this(NotificationType.FRIEND_REQUEST, request);
    }

    @Override
    public NotificationType getType() {
        return type;
    }   
}
