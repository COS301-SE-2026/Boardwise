package com.boardwise.backend.user_service.models.user_preferences;

import lombok.Data;

@Data 
public class Notifications {

    private boolean eventRSVPs;
    private boolean eventInvites;
    private boolean friendRequests;
    private boolean friendConfirmation;
    private boolean eventUpdates;
    private boolean directMessages;
    private boolean communityMessages;
    private boolean communityInvites;

    public Notifications(){
        this.eventRSVPs = true;
        this.eventInvites = true;
        this.eventUpdates = true;
        this.friendRequests = true;
        this.friendConfirmation = true;
        this.directMessages = true;
        this.communityMessages = true;
        this.communityInvites = true;
    }
}
