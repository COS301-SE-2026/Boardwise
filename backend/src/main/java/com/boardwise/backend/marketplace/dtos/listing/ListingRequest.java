package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingType;
import com.boardwise.backend.marketplace.model.RentalPeriod;

import jakarta.validation.constraints.*;

//NOTE: the record assumes status = ACTIVE on creation 
public record ListingRequest(
        @NotBlank String userId,
        @NotBlank String gameTitle,
        @NotNull ItemType itemType,
        @NotNull ListingType listingType,
        @Positive double price,
        String description,
        String imageUrl, // if image_url is null then use a default image based on game_id
        @NotEmpty List<String> genres,
        LocalDateTime[] rentalPeriod) {
}
