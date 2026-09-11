package com.boardwise.backend.user_service.events;

import org.springframework.context.ApplicationEvent;

public class JoinedCommunityEvent extends ApplicationEvent {

    private Object message;

    public JoinedCommunityEvent(Object source, Object message) {
        super(source);
        this.message = message;
    }

    public Object getMessage(){
        return message;
    }
}
