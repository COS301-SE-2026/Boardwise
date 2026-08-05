package com.boardwise.backend.user_service.models;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "BOARD_GAME")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Boardgame {
    @Id
    private String id;
    @Nullable
    private Integer bggId;
    @TextIndexed
    private String title;
    private String description;
    private String imageURL;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer minAge;
    private Integer duration;
    private List<String> genres;
}
