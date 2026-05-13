package com.boardwise.backend.marketplace.dtos.listing;

import java.time.LocalDateTime;
import java.util.List;

import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ListingResponse(
        String listing_id,
        String user_id,
        String game_title,
        ItemType item_type,
        ListingType listing_type,
        double price,
        String description,
        String image_url,
        LocalDateTime[] rental_period,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        ListingStatus status) {
}
