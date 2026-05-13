package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;
import com.boardwise.backend.marketplace.model.RentalPeriod;

public record ListingUpdateRequest(
        String game_title,
        ListingType listing_type,
        Double price, // make null if no changes were made
        String description,
        ListingStatus status,
        String image_url,
        List<String> genres,
        LocalDateTime[] rentalPeriod) {
}