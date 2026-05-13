package com.boardwise.backend.marketplace.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boardwise.backend.marketplace.enums.ListingType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//discuss if it should have its own collection or 
//it will be stored as an extra section underneath 
// @Document(collection = "Orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Data
public class Order {
    @Id
    private String id;

    @Field("listing_id")
    private String listing_id;

    @Field("buyer_id")
    private String buyer_id;

    @Field("total_price")
    private double total_price;

    @Field("rental_period")
    private RentalPeriod rental_period;

}
