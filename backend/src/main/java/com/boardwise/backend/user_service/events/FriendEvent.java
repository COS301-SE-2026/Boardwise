package com.boardwise.backend.user_service.events;

import org.springframework.context.ApplicationEvent;

import com.boardwise.backend.user_service.events.payload.FriendEventPayload;

public class FriendEvent extends ApplicationEvent {

    private FriendEventPayload message;

    public FriendEvent(Object source, FriendEventPayload message){
        super(source);
        this.message = message;
    }

    public FriendEventPayload getMessage(){
        return message;
    }
}
