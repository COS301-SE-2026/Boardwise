package com.boardwise.backend.shared.services.scoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.boardwise.backend.shared.model.Boardgame;

// score = (n/(n+k)) * internal + (k/(n+k)) * bgg
//
// n is data points for that one game, not how many users the platform has. that's the
// whole point: a game nobody has touched keeps leaning on bgg no matter how busy we
// get. with a platform-wide n every game would flip to internal weight at once,
// including ones with no internal score to give
//
// k is how much evidence buys parity, so at n = k the two contribute equally
@Component
@Primary
public class BlendedPopularityScorer implements PopularityScorer {

    private final BggPopularityScorer bggScorer;
    private final InternalPopularityScorer internalScorer;
    private final InternalEngagementSource engagementSource;
    private final double k;

    public BlendedPopularityScorer(
            BggPopularityScorer bggScorer,
            InternalPopularityScorer internalScorer,
            InternalEngagementSource engagementSource,
            @Value("${popularity.blend.k}") double k) {
        this.bggScorer = bggScorer;
        this.internalScorer = internalScorer;
        this.engagementSource = engagementSource;
        this.k = k;
    }

    @Override
    public Double score(Boardgame game) {
        Double bggScore = bggScorer.score(game);
        Double internalScore = internalScorer.score(game);

        if (internalScore == null)
            return bggScore;

        if (bggScore == null)
            return internalScore;   // user submitted game, no bggId, so no stats

        long n = engagementSource.forGame(game).dataPoints();
        double denominator = n + k;

        if (denominator <= 0.0)
            return internalScore;

        return (n / denominator) * internalScore + (k / denominator) * bggScore;
    }
}