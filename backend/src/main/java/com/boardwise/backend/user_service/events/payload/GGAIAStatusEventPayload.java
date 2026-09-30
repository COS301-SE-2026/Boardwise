package com.boardwise.backend.user_service.events.payload;

import com.boardwise.backend.user_service.dtos.notifications.GGAIAStatusNotification;

public record GGAIAStatusEventPayload(
    String receiverId,
    GGAIAStatusNotification notification
) implements UserRelatedEventPayload{}
