package com.boardwise.backend.user_service.models.user_preferences;

import com.boardwise.backend.user_service.enums.AppTheme;

import lombok.Data;

@Data 
public class Appearance {

    private AppTheme theme;

    public Appearance(){
        this.theme = AppTheme.LIGHT;
    }
}
