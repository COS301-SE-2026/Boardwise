package com.boardwise.backend.marketplace.dtos.order;

import java.time.LocalDateTime;

import com.boardwise.backend.marketplace.model.RentalPeriod;

import jakarta.validation.constraints.NotBlank;

// Note: needs to create date_of_purchase object
public record OrderRequest(
        @NotBlank String buyer_id,
        @NotBlank String listing_id,
        LocalDateTime[] rental_period) {
}
