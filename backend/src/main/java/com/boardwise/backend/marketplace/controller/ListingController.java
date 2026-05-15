package com.boardwise.backend.marketplace.controller;

import com.boardwise.backend.marketplace.repository.ListingRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.service.*;

import jakarta.validation.*;

import java.util.*;

@RestController
@RequestMapping("/api/marketplace")

public class ListingController {
    private final ListingRepository listingRepository;
    private final ListingService listingService;

    public ListingController(ListingService listingService, ListingRepository listingRepository) {
        this.listingService = listingService;
        this.listingRepository = listingRepository;
    }

    // AC-MKT-01: Get All Active Listings
    @GetMapping("/listings")
    public ResponseEntity<List<ListingResponse>> getAllListings() {
        try {
            List<ListingResponse> listings = listingService.getAllActiveListings();
            if (listings.isEmpty()) {
                return ResponseEntity.status(204).body(null);
            }

            return ResponseEntity.status(200).body(listings);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(listingService.getAllActiveListings());

        }
    }

    // AC-MKT-02: Get Listing by ID
    @GetMapping("/listings/{listingId}") // to be changed
    public ResponseEntity<ListingResponse> getListingById(@PathVariable String listingId) {
        // TODO: process GET request
        try {
            return ResponseEntity.status(200).body(listingService.getListingById(listingId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(listingService.getListingById(listingId));
        }

    }

    // AC-MKT-03: Create a Listing
    @PostMapping("/listings")
    public ResponseEntity<ListingResponse> createListing(@RequestBody @Valid ListingRequest req) {
        try {
            ListingResponse response = listingService.createListing(req);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(422).body(null);
        }
    }

    // AC-MKT-04: Update a Listing
    @PatchMapping("/listings/{listingId}")
    public String updateListing(@RequestBody String entity) {
        // TODO: process PATCH request

        return null;
    }

    // AC-MKT-05: Delete a Listing
    @DeleteMapping("/listings/{listingId}")
    public ResponseEntity<String> deleteListing(@PathVariable String listingId) {
        // TODO: process DELETE request
        try {
            listingService.deleteListing(listingId);
            return ResponseEntity.status(204).body("");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Listing not found");
        }
    }

    // AC-MKT-06: Get Authenticated User's Listings
    @GetMapping("/listings/user") // to be changed
    public String getUserListings(@RequestParam String param) {
        // TODO: process GET request
        return null;
    }

    // TODO: AC-MKT-07: Get Retail Sources for a Game Title

}
