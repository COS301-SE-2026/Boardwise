package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;

import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.RentalPeriod;

public record ListingResponse(
        String listingId,
        String listingTitle,
        String username,
        ObjectId userId,
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
