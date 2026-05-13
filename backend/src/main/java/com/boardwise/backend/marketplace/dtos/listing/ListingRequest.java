package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingType;
import com.boardwise.backend.marketplace.model.RentalPeriod;

import jakarta.validation.constraints.*;

//NOTE: the record assumes status = ACTIVE on creation 
public record ListingRequest(
        @NotBlank String user_id,
        @NotBlank String game_title,
        @NotNull ItemType item_type,
        @NotNull ListingType listing_type,
        @Positive double price,
        String description,
        String image_url, // if image_url is null then use a default image based on game_id
        @NotEmpty List<String> genres,
        LocalDateTime[] rental_period) {
}
