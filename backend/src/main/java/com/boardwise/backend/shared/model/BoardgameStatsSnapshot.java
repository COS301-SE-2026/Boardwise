package com.boardwise.backend.shared.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// one row per game per refresh.
// bgg only gives current counts and has no history endpoint, so anything time based
// (momentum, trending, growth) has to come from snapshots we take ourselves.
// can't be backfilled later
@Document(collection = "BOARD_GAME_STATS_SNAPSHOT")
@CompoundIndex(name = "bggId_capturedAt", def = "{'bggId': 1, 'capturedAt': -1}")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardgameStatsSnapshot {
    @Id
    private String id;

    @Indexed
    private Integer bggId;

    private Instant capturedAt;

    private Integer owned;
    private Integer usersRated;
    private Double bayesAverage;
    private Double popularityScore;
}