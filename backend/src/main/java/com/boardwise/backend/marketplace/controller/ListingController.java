package com.boardwise.backend.marketplace.controller;

import com.boardwise.backend.marketplace.repository.ListingRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.service.*;

import jakarta.validation.*;

import java.util.*;

@RestController
@RequestMapping("/api/marketplace")

public class ListingController {
    

    
    private final ListingService listingService;

    public ListingController(ListingService listingService, ListingRepository listingRepository) {
        this.listingService = listingService;
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
    @GetMapping("/users/{userId}/listings") // to be changed
    public ResponseEntity<ListingResponse> getListingById(@PathVariable String userId) {
        try {
            return ResponseEntity.status(200).body(listingService.getListingById(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }

    }

    // AC-MKT-03: Create a Listing
    @PostMapping(value = "/listings", consumes = "multipart/form-data")
    public ResponseEntity<ListingResponse> createListing(
            @RequestPart("data") @Valid ListingRequest req,
            @RequestPart("image") MultipartFile img,
            @RequestHeader("Authorization") String token) {
        try {
            ListingResponse response = listingService.createListing(req, token.replace("Bearer ", ""), img);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            System.out.println(e.toString());

            return ResponseEntity.status(422).body(null);
        }
    }

    // AC-MKT-04: Update a Listing
    @PatchMapping("/listings/{listingId}")
    public ResponseEntity<ListingResponse> updateListing(
            @RequestBody ListingRequest req,
            @PathVariable String listingId,
            @RequestHeader("Authorization") String token) {
        try {
            ListingResponse updated = listingService.updateListing(listingId, req, token.replace("Bearer ", ""));
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // AC-MKT-05: Delete a Listing
    @DeleteMapping("/listings/{listingId}")
    public ResponseEntity<Void> deleteListing(
            @PathVariable String listingId,
            @RequestHeader("Authorization") String token) {
        try {
            listingService.deleteListing(listingId, token.replace("Bearer ", ""));
            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // AC-MKT-06: Get Authenticated User's Listings
    @GetMapping("/listings/{user}")
    public ResponseEntity<List<ListingResponse>> getUserListings(@PathVariable String user) {
        // TODO: process GET request
        try {
            List<ListingResponse> listings = listingService.getUserListings(user);
            if (listings.isEmpty()) {
                return ResponseEntity.status(204).body(null);
            }

            return ResponseEntity.status(200).body(listings);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);

        }
    }

    @GetMapping("/listings/search")
    public ResponseEntity<List<ListingResponse>> getFilteredListings(@RequestParam(required = false) String listingType,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> genres) {
        try {
            List<ListingResponse> listings = listingService.getByFilter(listingType, itemType, minPrice, maxPrice,
                    genres);
            if (listings.isEmpty()) {
                return ResponseEntity.status(204).body(null);
            }
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}
