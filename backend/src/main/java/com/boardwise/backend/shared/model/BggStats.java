package com.boardwise.backend.shared.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// the <statistics> block bgg returns when stats=1 is on the request.
// embedded in Boardgame instead of flattened so the game document stays readable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BggStats {
    private Integer owned;
    private Integer usersRated;
    private Double average;
    private Double bayesAverage;
    private Integer wishing;
    private Integer wanting;
    private Integer trading;
    private Integer numComments;
}