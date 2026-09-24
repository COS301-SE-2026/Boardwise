package com.boardwise.backend.shared.model;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
    @Indexed(unique = true, sparse = true) 
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

    @Nullable 
    private Integer yearPublished;
    @Nullable
    private BggStats stats;
    @Nullable
    private Double popularityScore;

    // null until the refresh job has been here. drives the refresh cursor
    @Nullable
    @Indexed
    private Instant lastStatsRefreshedAt;

    public Boardgame(String id, Integer bggId, String title, String description, String imageURL,
                     Integer minPlayers, Integer maxPlayers, Integer minAge, Integer duration,
                     List<String> genres) {
        this.id = id;
        this.bggId = bggId;
        this.title = title;
        this.description = description;
        this.imageURL = imageURL;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.minAge = minAge;
        this.duration = duration;
        this.genres = genres;
    }
}
