package com.boardwise.backend.user_service.dtos.notifications;

import com.boardwise.backend.user_service.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record PresenceNotification(
    NotificationType type,
    String userId,
    @JsonProperty("isOnline") boolean isOnline
) implements NotificationDTO {

    public PresenceNotification(String userId, boolean isOnline){
        this(NotificationType.PRESENCE, userId, isOnline);
    }

    @Override
	public NotificationType getType(){
        return type;
    }
}
