package com.boardwise.backend.marketplace.dtos.listing;

import java.util.List;

import jakarta.validation.constraints.*;

//NOTE: the record assumes status = ACTIVE on creation 
public record ListingRequest(
        @NotNull String itemType,
        @NotNull String listingType,
        @NotNull String listingTitle,
        @Positive double price,
        @NotBlank String gameTitle,
        @NotBlank String location,
        boolean isNegotiable,
        String imageUrl,
        @NotBlank String version, 
        @NotBlank String condition,
        @NotBlank String description,
        @NotEmpty List<String> genres,
        List<String> rentalPeriod) {
}
