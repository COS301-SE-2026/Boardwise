package com.boardwise.backend.marketplace.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boardwise.backend.marketplace.enums.ListingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "LISTINGS")
public class Listing {

    @Id
    private String id;

    @Field("username")
    private String username;

    @Field("item_type")
    private String itemType;

    @Field("listing_type")
    private String listingType;

    @Field("price")
    private double price;

    @Field("game_title")
    private String title;

    @Field("description")
    private String description;

    @Field("image_url")
    private String imageUrl;

    @Field("status")
    private ListingStatus status;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("genres")
    private List<String> genres;

    @Field("rental_period")
    private RentalPeriod rentalPeriod;
}
