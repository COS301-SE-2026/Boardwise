package com.boardwise.backend.user_service.models;

import com.boardwise.backend.user_service.dtos.FriendRequestDTO;

/**
 * FriendRequestData
 */
public final record FriendRequestData(
    FriendRequestDTO request
) implements NotificationData {}
