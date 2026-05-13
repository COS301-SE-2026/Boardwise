package com.boardwise.backend.marketplace.dtos.order;

import java.time.LocalDateTime;

import com.boardwise.backend.marketplace.enums.OrderStatus;
import com.boardwise.backend.marketplace.model.RentalPeriod;

public record OrderResponse(
        String orderId,
        String buyerId,
        String listingId,
        OrderStatus status,
        String sellerId,
        double totalPrice,
        RentalPeriod rentalPeriod, // not in all responses
        LocalDateTime dateOfPurchase) {

}
