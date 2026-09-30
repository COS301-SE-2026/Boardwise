package com.boardwise.backend.user_service.events;

import org.springframework.context.ApplicationEvent;
import com.boardwise.backend.user_service.events.payload.UserRelatedEventPayload;

public abstract class UserRelatedEvent extends ApplicationEvent{

    private UserRelatedEventPayload message;

    public UserRelatedEvent(Object source, UserRelatedEventPayload message) {
        super(source);
        this.message = message;
    } 

    public UserRelatedEventPayload getMessage(){
        return message;
    }

}
