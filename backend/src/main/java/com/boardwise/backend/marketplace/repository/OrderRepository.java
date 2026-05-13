package com.boardwise.backend.marketplace.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.marketplace.model.Order;
import java.util.List;
import com.boardwise.backend.marketplace.enums.OrderStatus;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByBuyerId(String buyerId);

    List<Order> findByListingId(String listingId);

    List<Order> findBySellerId(String sellerId);

    List<Order> findByStatus(OrderStatus status);
}
