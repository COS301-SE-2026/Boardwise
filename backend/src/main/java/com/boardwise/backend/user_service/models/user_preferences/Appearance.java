package com.boardwise.backend.user_service.models.user_preferences;

import com.boardwise.backend.user_service.enums.AppTheme;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor 
@Getter 
@Setter  
@EqualsAndHashCode 
public class Appearance {

    private AppTheme theme;

    public Appearance(){
        this.theme = AppTheme.LIGHT;
    }
}
