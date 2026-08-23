package com.boardwise.backend.user_service.dtos;

public record InviteNotification(
    String type,
    EventHostInfo host,
    EventInviteInfo event
) implements NotificationDTO {

    public InviteNotification(EventHostInfo host, EventInviteInfo event){
        this("EVENT_INVITE", host, event);
    }

    @Override
    public String getType() {
        return type;
    }
}
