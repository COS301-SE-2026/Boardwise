package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.dtos.EventHostInfo;
import com.boardwise.backend.user_service.dtos.EventInviteInfo;
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
