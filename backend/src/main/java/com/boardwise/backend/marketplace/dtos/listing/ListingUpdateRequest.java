package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.Condition;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;

public record ListingUpdateRequest(
        String gameIitle,
        ListingType listingType,
        Double price,
        String description,
        ListingStatus status,
        String location,
        boolean isNegotiable,
        Condition condition,
        String version,
        String imageUrl,
        List<String> genres,
        LocalDateTime[] rentalPeriod) {
}