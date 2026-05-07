package com.boardwise.backend.marketplace.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "Listings")
public class Listing {
    @Id
    private String id;

    @Field("user_id")
    private String user_id;

    @Field("game_id")
    private String game_id;

    @Field("item_type")
    private String item_type;

    @Field("listing_type")
    private String listing_type;

    @Field("price")
    private double price;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("image_url")
    private String image_url;

    @Field("status")
    private String status;

    @Field("created_at")
    private Date created_at;

    @Field("updated_at")
    private Date updated_at;

    @Field("genres")
    private List<String> genres;

}
