package com.boardwise.backend.user_service.events.payload;

import com.boardwise.backend.user_service.dtos.notifications.NotificationDTO;

public sealed interface UserRelatedEventPayload 
permits FriendEventPayload, GGAIAStatusEventPayload {
    String receiverId();
    NotificationDTO notification();
}
