package com.boardwise.backend.marketplace.dtos.listing;

import java.util.List;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.models.RentalPeriod;

public record ListingResponse(
        String listingId,
        String listingTitle,
        String username,
        String userId,
        String gameTitle,
        String itemType,
        String listingType,
        double price,
        String description,
        String imageUrl,
        String location,
        Boolean isNegotiable,
        String condition,
        String version,
        List<String> genres,
        RentalPeriod rentalPeriod,
        ListingStatus status) {}
