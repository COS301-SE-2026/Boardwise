package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;

public record ListingUpdateRequest(
        String gameIitle,
        ListingType listingType,
        Double price, // make null if no changes were made
        String description,
        ListingStatus status,
        String imageUrl,
        List<String> genres,
        LocalDateTime[] rentalPeriod) {
}