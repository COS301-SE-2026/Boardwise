package com.boardwise.backend.marketplace.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.data.domain.Page;
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

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    // AC-MKT-01: Get All Active Listings
    @GetMapping("/listings")
    public ResponseEntity<List<ListingResponse>> getAllListings() {
        try {
            List<ListingResponse> listings = listingService.getAllActiveListings();
            if (listings.isEmpty()) {
                return ResponseEntity.accepted().body(null);
            }

            return ResponseEntity.ok().body(listings);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);

        }
    }

    // AC-MKT-02: Get Listing by ID
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<ListingResponse> getListingById(@PathVariable String listingId) {
        try {
            return ResponseEntity.ok(listingService.getListingById(listingId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
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
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    // AC-MKT-04: Update a Listing
    @PatchMapping(value = "/listing/{listingId}", consumes = "multipart/form-data")
    public ResponseEntity<ListingResponse> updateListing(
            @RequestPart("data") @Valid ListingRequest req,
            @RequestPart(value = "image", required = false) MultipartFile img,
            @PathVariable String listingId,
            @RequestHeader("Authorization") String token) {
        try {
            ListingResponse updated = listingService.updateListing(listingId, req, token.replace("Bearer ", ""), img);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            System.err.println("UPDATE ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (ForbiddenException e) {
            System.err.println("Forbidden Exception -- UPDATE ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("UPDATE ERROR: " + e.getClass().getName() + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // AC-MKT-05: Delete a Listing
    @DeleteMapping("/listing/{listingId}")

    public ResponseEntity<Void> deleteListing(
            @PathVariable String listingId,
            @RequestHeader("Authorization") String token) {
        try {
            listingService.deleteListing(listingId, token.replace("Bearer ", ""));
            
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // AC-MKT-06: Get Authenticated User's Listings
    @GetMapping("/listings/user")
    public ResponseEntity<List<ListingResponse>> getUserListings(@RequestHeader("Authorization") String token) {
        try {
            List<ListingResponse> listings = listingService.getUserListings(token.replace("Bearer ", ""));
            if (listings.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(listings);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/listings/search")
    public ResponseEntity<Page<ListingResponse>> getFilteredListings(
            @RequestParam(required = false) String gameTitle,
            @RequestParam(required = false) String listingTitle,
            @RequestParam(required = false) String listingType,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> conditions,
            @RequestParam(required = false) List<String> genres,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
            ) {
        try {
            Page<ListingResponse> listings = listingService.getByFilter(gameTitle, listingTitle, listingType, itemType, minPrice, maxPrice,
                    conditions,genres, page, size);
            if (listings.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
