package com.boardwise.backend.user_service.models;

/**
 * ChatMessageData
 */
public final record ChatMessageData(
    String senderId,
    String message
) implements NotificationData {}
