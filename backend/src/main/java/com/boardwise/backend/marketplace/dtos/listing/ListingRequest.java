package com.boardwise.backend.marketplace.dtos.listing;

import java.util.List;

import jakarta.validation.constraints.*;

//NOTE: the record assumes status = ACTIVE on creation 
public record ListingRequest(
        @NotBlank String userId,
        @NotBlank String gameId,
        @NotNull String itemType,
        @NotNull String listingType,
        @Positive double price,
        @NotBlank String gameTitle,
        @NotBlank String description,
        @NotBlank String imageUrl, // if image_url is null then use a default image based on game_id
        @NotEmpty List<String> genres,
        List<String> rentalPeriod) {
}
