package com.boardwise.backend.user_service.models.user_preferences;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Preferences {
    private List<String> genres;
    private Settings settings;

    public Preferences(){
        genres = new ArrayList<>();
        settings = new Settings();
    }
}
