package com.boardwise.backend.user_service.events;

import org.springframework.context.ApplicationEvent;

import com.boardwise.backend.user_service.events.payload.JoinedCommunityEventPayload;

public class JoinedCommunityEvent extends ApplicationEvent {

    private JoinedCommunityEventPayload message;

    public JoinedCommunityEvent(Object source, JoinedCommunityEventPayload message) {
        super(source);
        this.message = message;
    }

    public JoinedCommunityEventPayload getMessage(){
        return message;
    }
}
