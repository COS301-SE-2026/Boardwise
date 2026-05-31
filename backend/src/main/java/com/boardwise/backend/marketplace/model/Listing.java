package com.boardwise.backend.marketplace.model;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
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

    @Field("userId")
    private ObjectId userId;

    @Field("itemType")
    private String itemType;

    @Field("listingType")
    private String listingType;

    @Field("price")
    private double price;

    @Field("location")
    private String location;

    //TODO: Add to ERD
    @Field("isNegotiable")
    private Boolean isNegotiable;

    //TODO: Add to ERD
    @Field("condition")
    private String condition;

    //TODO: Make sure it goes through the pipeline
    @Field("gameTitle")
    private String title;

    //TODO: Add to ERD AND COMPARE AGAINST 
    @Field("version")
    private String version;

    @Field("description")
    private String description;

    @Field("imageUrl")
    private String imageUrl;

    @Field("status")
    private ListingStatus status;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;

    @Field("genres")
    private List<String> genres;

    @Field("rentalPeriod")
    private RentalPeriod rentalPeriod;
}
