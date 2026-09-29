package com.boardwise.backend.user_service.events.payload;

import com.boardwise.backend.user_service.dtos.notifications.CommunityMessageNotification;

public record JoinedCommunityEventPayload(
    String communityId,
    CommunityMessageNotification notification
) {}
