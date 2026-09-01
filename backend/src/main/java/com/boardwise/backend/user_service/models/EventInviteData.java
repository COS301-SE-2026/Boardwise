package com.boardwise.backend.user_service.models;

import com.boardwise.backend.user_service.dtos.EventHostInfo;
import com.boardwise.backend.user_service.dtos.EventInviteInfo;

/**
 * EventInviteData
 */
public final record EventInviteData(
    EventHostInfo host,
    EventInviteInfo event
) implements NotificationData {}
