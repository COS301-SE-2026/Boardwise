package com.boardwise.backend.user_service.events;

import com.boardwise.backend.user_service.events.payload.FriendEventPayload;

public class FriendEvent extends UserRelatedEvent {

    public FriendEvent(Object source, FriendEventPayload message){
        super(source, message);
    }
}
