package com.boardwise.backend.marketplace.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class MarketplaceControllers {

    // AC-MKT-01: Get All Active Listings
    @GetMapping("/api/marketplace/listings")
    public String getAllListings(@RequestParam String param) {
        // TODO: process GET request

        return null;
    }

    // AC-MKT-02: Get Listing by ID
    @GetMapping("/api/marketplace/listings/{listingId}") // to be changed
    public String getListingById(@RequestParam String param) {
        // TODO: process GET request
        return null;
    }

    // AC-MKT-03: Create a Listing
    @PostMapping("/api/marketplace/listings")
    public String createListing(@RequestBody String entity) {
        // TODO: process POST request

        return null;
    }

    // AC-MKT-04: Update a Listing
    @PatchMapping("/api/marketplace/listings/{listingId}")
    public String updateListing(@RequestBody String entity) {
        // TODO: process PATCH request

        return null;
    }

    // AC-MKT-05: Delete a Listing
    @DeleteMapping("/api/marketplace/listings/{listingId}")
    public String deleteListing(@RequestBody String entity) {
        // TODO: process DELETE request

        return null;
    }

    // AC-MKT-06: Get Authenticated User's Listings
    @GetMapping("/api/marketplace/listings/{listingId}") // to be changed
    public String getUserListings(@RequestParam String param) {
        // TODO: process GET request
        return null;
    }

    // TODO: AC-MKT-07: Get Retail Sources for a Game Title 
}
