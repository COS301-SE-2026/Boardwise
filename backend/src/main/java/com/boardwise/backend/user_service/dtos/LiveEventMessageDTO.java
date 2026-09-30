package com.boardwise.backend.user_service.dtos;

import com.boardwise.backend.user_service.models.LiveEventMessage;

public record LiveEventMessageDTO(
    String id, String eventId, String senderId, String senderUsername,
    String content, boolean isHost, String createdAt
) {
    public static LiveEventMessageDTO from(LiveEventMessage m) {
        return new LiveEventMessageDTO(
            m.id(), m.eventId(), m.senderId(), m.senderUsername(),
            m.content(), m.isHost(), m.createdAt().toString());
    }
}