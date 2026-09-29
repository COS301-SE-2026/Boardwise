package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.enums.NotificationType;

public sealed interface NotificationDTO 
permits InviteNotification, ChatNotification, FriendConfirmationNotification, 
FriendRequestNotification, PresenceNotification, UnfriendNotification{
    NotificationType getType();
}
