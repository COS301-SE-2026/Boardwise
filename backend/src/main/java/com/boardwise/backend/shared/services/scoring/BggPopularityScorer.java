package com.boardwise.backend.shared.services.scoring;

import java.time.Year;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boardwise.backend.shared.model.BggStats;
import com.boardwise.backend.shared.model.Boardgame;

// popularity from bgg stats, scaled 0..1
// each term gets normalised before weighting, so the weights sum to 1 and each one
// is just its share of influence. raw they aren't comparable: log(owned+1) goes up
// to about 12 but bayesaverage is stuck between 5.5 and 6.5 for most games
@Component
public class BggPopularityScorer implements PopularityScorer {

    // normalise against fixed points, not catalogue min/max or a z-score. those move
    // with the population, so adding games would change the score of every game
    // already stored
    private static final double REF_LOG_OWNED = 12.2;           // ln(200 000)
    private static final double REF_LOG_USERS_RATED = 11.9;     // ln(150 000)
    private static final double REF_LOG_OWNED_PER_YEAR = 8.7;   // ln(6000)

    private static final double BAYES_FLOOR = 5.5;              // bgg's prior pulls everything here
    private static final double BAYES_CEILING = 8.5;

    private final double w1;
    private final double w2;
    private final double w3;
    private final double w4;

    public BggPopularityScorer(
            @Value("${popularity.weights.owned}") double w1,
            @Value("${popularity.weights.users-rated}") double w2,
            @Value("${popularity.weights.bayes-average}") double w3,
            @Value("${popularity.weights.owned-per-year}") double w4) {
        this.w1 = w1;
        this.w2 = w2;
        this.w3 = w3;
        this.w4 = w4;
    }

    @Override
    public Double score(Boardgame game) {
        BggStats stats = game.getStats();
        if (stats == null)
            return null;

        int owned = stats.getOwned() != null ? stats.getOwned() : 0;
        int usersRated = stats.getUsersRated() != null ? stats.getUsersRated() : 0;

        // bgg sends 0 for games under its ranking threshold, so that's a real value
        double bayes = stats.getBayesAverage() != null ? stats.getBayesAverage() : 0.0;

        return w1 * normalisedLog(owned, REF_LOG_OWNED)
                + w2 * normalisedLog(usersRated, REF_LOG_USERS_RATED)
                + w3 * normalisedBayes(bayes)
                + w4 * normalisedLog(ownedPerYear(game, owned), REF_LOG_OWNED_PER_YEAR);
    }

    // owned is cumulative, so without this an old game beats a new one just for having
    // been around longer. set w4 to 0 to drop the correction
    private double ownedPerYear(Boardgame game, int owned) {
        Integer published = game.getYearPublished();

        // bgg uses 0 for unknown, and carries some future-dated unreleased entries
        if (published == null || published <= 0)
            return owned;

        int age = Year.now(ZoneOffset.UTC).getValue() - published;
        return owned / (double) Math.max(1, age);
    }

    private double normalisedLog(double value, double reference) {
        return clamp(Math.log(value + 1.0) / reference);
    }

    private double normalisedBayes(double bayes) {
        if (bayes <= 0.0)
            return 0.0;   // unranked, don't let the floor drag it negative

        return clamp((bayes - BAYES_FLOOR) / (BAYES_CEILING - BAYES_FLOOR));
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}