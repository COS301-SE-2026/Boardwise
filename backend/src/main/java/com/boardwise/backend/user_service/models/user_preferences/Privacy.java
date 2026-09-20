package com.boardwise.backend.user_service.models.user_preferences;

import com.boardwise.backend.user_service.enums.Visibility;

import lombok.Data;

@Data 
public class Privacy {

    private Visibility visibility;
    private boolean showFriendsList;
    private boolean showPreferences;
    private boolean showEvents;
    private boolean showOnlineStatus;

    public Privacy(){
        this.visibility = Visibility.PUBLIC;
        this.showFriendsList = true;
        this.showPreferences = true;
        this.showEvents = true;
        this.showOnlineStatus = true;
    }
}
