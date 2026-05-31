package com.boardwise.backend.marketplace.dtos.listing;

import java.util.List;
import java.util.concurrent.locks.Condition;

import jakarta.validation.constraints.*;

//NOTE: the record assumes status = ACTIVE on creation 
public record ListingRequest(
        @NotNull String itemType,
        @NotNull String listingType,
        @Positive double price,
        @NotBlank String gameTitle,
        @NotBlank String location,
        boolean isNegotiable,
        @NotBlank String version, 
        @NotBlank String condition,
        @NotBlank String description,
        @NotEmpty List<String> genres,
        List<String> rentalPeriod) {
}
