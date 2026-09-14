package com.boardwise.backend.user_service.dtos.notifications;

import java.util.List;

public record NotificationsDTO(
    String message,
    List<NotificationDTO> notifications
) {}
