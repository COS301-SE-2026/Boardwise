package com.boardwise.backend.user_service.models;

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
    private String visibility;
    private List<String> genres;

    public Preferences(){
        visibility = "public";
        genres = new ArrayList<>();
    }
}
