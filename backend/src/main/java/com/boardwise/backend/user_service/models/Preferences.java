package com.boardwise.backend.user_service.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Preferences {
    @JsonProperty("isPrivate")
    private boolean isPrivate;
    private List<String> genres;

    public Preferences(){
        isPrivate = false;
        genres = new ArrayList<>();
    }
}
