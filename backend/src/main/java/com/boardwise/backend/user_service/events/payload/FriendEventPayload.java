package com.boardwise.backend.user_service.events.payload;

import com.boardwise.backend.user_service.dtos.notifications.NotificationDTO;

public record FriendEventPayload(
    String receiverId,
    NotificationDTO notification
) {}
