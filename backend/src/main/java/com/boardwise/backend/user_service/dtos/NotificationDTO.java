package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.NotificationType;

public sealed interface NotificationDTO 
permits InviteNotification, ChatNotification, FriendConfirmationNotification, 
FriendRequestNotification{
    NotificationType getType();
}
