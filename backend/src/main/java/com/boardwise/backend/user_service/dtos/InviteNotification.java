package com.boardwise.backend.user_service.dtos;

public record InviteNotification(
    String type,
    String message
) implements Notification {

    @Override
    public String getType() {
        return type;
    }
}
