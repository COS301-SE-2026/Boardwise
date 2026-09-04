package com.boardwise.backend.user_service.models;

import com.boardwise.backend.user_service.dtos.FriendDTO;

/**
 * FriendConfirmationData
 */
public final record FriendConfirmationData(
    FriendDTO friend
) implements NotificationData {}
