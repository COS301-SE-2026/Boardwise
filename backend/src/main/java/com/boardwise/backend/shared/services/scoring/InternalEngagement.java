package com.boardwise.backend.shared.services.scoring;

public record InternalEngagement(
    long dataPoints, 
    int owned, 
    int contactInquiries
) {

    public static final InternalEngagement NONE = new InternalEngagement(0L, 0, 0);

    public boolean isEmpty() {
        return dataPoints <= 0L;
    }
}