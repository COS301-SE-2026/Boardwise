package com.boardwise.backend.marketplace.dtos.order;

import java.time.LocalDateTime;

import com.boardwise.backend.marketplace.enums.OrderStatus;
import com.boardwise.backend.marketplace.model.RentalPeriod;

public record OrderResponse(
        String order_id,
        String buyer_id,
        String listing_id,
        OrderStatus status,
        String seller_id,
        double total_price,
        RentalPeriod rental_period, // not in all responses
        LocalDateTime date_of_purchase) {

}
