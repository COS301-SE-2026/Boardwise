package com.boardwise.backend.user_service.models.user_preferences;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Settings {

    private Appearance appearance;
    private Notifications notifications;
    private Privacy privacy;

    public Settings(){
        this.appearance = new Appearance();
        this.notifications = new Notifications();
        this.privacy = new Privacy();
    }
}
