package com.boardwise.backend.shared.services.scoring;

import com.boardwise.backend.shared.model.Boardgame;

public interface PopularityScorer {
    Double score(Boardgame game);
}