package com.boardwise.backend.user_service.events;

import com.boardwise.backend.user_service.events.payload.GGAIAStatusEventPayload;

public class GGAIAStatusEvent extends UserRelatedEvent{
    public GGAIAStatusEvent(Object source, GGAIAStatusEventPayload message) {
        super(source, message);
    }
}
