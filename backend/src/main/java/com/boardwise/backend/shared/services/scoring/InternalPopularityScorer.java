package com.boardwise.backend.shared.services.scoring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boardwise.backend.shared.model.Boardgame;

// internal_score = wX * log(owned + 1) + wY * log(contact_inquiries + 1)
// scaled 0..1 like the bgg score so the two blend without one swamping the other
@Component
public class InternalPopularityScorer implements PopularityScorer {

    private final InternalEngagementSource engagementSource;
    private final double wOwned;
    private final double wInquiries;
    private final double refLogOwned;
    private final double refLogInquiries;

    public InternalPopularityScorer(
            InternalEngagementSource engagementSource,
            @Value("${popularity.internal.weights.owned}") double wOwned,
            @Value("${popularity.internal.weights.contact-inquiries}") double wInquiries,
            @Value("${popularity.internal.reference.owned}") int referenceOwned,
            @Value("${popularity.internal.reference.contact-inquiries}") int referenceInquiries) {
        this.engagementSource = engagementSource;
        this.wOwned = wOwned;
        this.wInquiries = wInquiries;
        this.refLogOwned = Math.log(referenceOwned + 1.0);
        this.refLogInquiries = Math.log(referenceInquiries + 1.0);
    }

    @Override
    public Double score(Boardgame game) {
        InternalEngagement engagement = engagementSource.forGame(game);
        if (engagement.isEmpty())
            return null;

        return wOwned * normalisedLog(engagement.owned(), refLogOwned)
                + wInquiries * normalisedLog(engagement.contactInquiries(), refLogInquiries);
    }

    private double normalisedLog(int value, double reference) {
        if (reference <= 0.0)
            return 0.0;

        return Math.max(0.0, Math.min(1.0, Math.log(value + 1.0) / reference));
    }
}