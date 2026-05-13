package com.boardwise.backend.marketplace.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Document(collection = "Listings")
public class Listing {

    @Id
    private String id;

    @Field("user_id")
    private String user_id;

    @Field("game_id")
    private String game_id;

    @Field("item_type")
    private ItemType item_type;

    @Field("listing_type")
    private ListingType listing_type;

    @Field("price")
    private double price;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("image_url")
    private String image_url;

    @Field("status")
    private ListingStatus status;

    @Field("created_at")
    private LocalDateTime created_at;

    @Field("updated_at")
    private LocalDateTime updated_at;

    @Field("genres")
    private List<String> genres;

    @Field("rental_period")
    private RentalPeriod rental_period;
}
