package com.boardwise.backend.shared.services.scoring;

import org.springframework.stereotype.Component;

import com.boardwise.backend.shared.model.Boardgame;

@Component
public class NoInternalEngagementSource implements InternalEngagementSource {

    @Override
    public InternalEngagement forGame(Boardgame game) {
        return InternalEngagement.NONE;
    }
}