package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.*;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;
import com.boardwise.backend.marketplace.model.RentalPeriod;

public record ListingResponse(
        String listingId,
        String userId,
        String gameTitle,
        ItemType itemType,
        ListingType listingType,
        double price,
        String description,
        String imageUrl,
        List<Genres> genres,
        RentalPeriod rentalPeriod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ListingStatus status) {
}
