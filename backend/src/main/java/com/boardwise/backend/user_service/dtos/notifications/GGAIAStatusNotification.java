package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.enums.NotificationType;

public record GGAIAStatusNotification(
    NotificationType type,
    String status,
    String reason
) implements NotificationDTO{

    public GGAIAStatusNotification(String status, String reason){
        this(NotificationType.GGAIASTATUS, status, reason);
    }

    @Override
    public NotificationType getType() {
        return type;
    }
}
