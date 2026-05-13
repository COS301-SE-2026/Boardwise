package com.boardwise.backend.marketplace.dtos.listing;

import java.util.List;

import com.boardwise.backend.marketplace.enums.*;

import jakarta.validation.constraints.*;

//NOTE: the record assumes status = ACTIVE on creation 
public record ListingRequest(
                @NotBlank String userId,
                @NotBlank String gameId,
                @NotNull ItemType itemType,
                @NotNull ListingType listingType,
                @Positive double price,
                @NotBlank String gameTitle,
                String description,
                String imageUrl, // if image_url is null then use a default image based on game_id
                @NotEmpty List<Genres> genres,
                String[] rentalPeriod) {
}
