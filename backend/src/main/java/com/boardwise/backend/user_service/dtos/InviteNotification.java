package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.enums.NotificationType;

public record InviteNotification(
    NotificationType type,
    EventHostInfo host,
    EventInviteInfo event
) implements NotificationDTO {

    public InviteNotification(EventHostInfo host, EventInviteInfo event){
        this(NotificationType.EVENT_INVITE, host, event);
    }

    @Override
    public NotificationType getType() {
        return type;
    }
}
