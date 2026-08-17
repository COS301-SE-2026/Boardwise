package com.boardwise.backend.marketplace.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import com.boardwise.backend.SharedMongoContainer;
import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.model.RentalPeriod;
import com.boardwise.backend.marketplace.repository.ListingRepository;

@DisplayName("Listing Repository Tests")
@DataMongoTest()
public class ListingRepositoryTest extends SharedMongoContainer {
    @Autowired
    private ListingRepository listingRepository;

    private final String defaultIMG = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/default-listing-images/default.png";

    private Listing buildListing(String id, String username, ObjectId objectId, String gameTitle, String listingType,
            double price, List<String> genres) {
        RentalPeriod rp = null;
        if (listingType.equals("rental")) {
            rp = new RentalPeriod();
            rp.setStartDate(LocalDate.now().plusDays(6));
            rp.setEndDate(LocalDate.now().plusDays(10));
        }

        return new Listing(id, username, objectId, "full boardgame", listingType, price,
        "Pretoria", false, "fake title", "like new", gameTitle, "original",
        "some description", defaultIMG, ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
        genres, rp);
    }

    private final ObjectId compId = new ObjectId();

    @DisplayName("Reset Repository")
    @BeforeEach
    void resetToSomeBaseTest() {
        listingRepository.deleteAll();
        Listing listing_ = buildListing("LARPING", "testBuddy", compId, "Monopoly", "sale", 500,
                List.of(Genres.CITY_BUILDING.getValue()));
        Listing listing_2 = buildListing("LARPING_2", "ineedAUniqueName", new ObjectId(), "Ludo", "rental", 100,
                List.of(Genres.ADVENTURE.getValue()));
        Listing listing_3 = buildListing("LARPING_3_ULTRA_LARPIST", "WhatBroSaid", new ObjectId(), "chess", "sale", 822,
                List.of(Genres.BOOK.getValue()));
        Listing listing_4 = buildListing("LARPING_4_ULTRA_LARPIST_FINAL_FORM", "whateverThatMeans", new ObjectId(),
                "Catan", "sale", 235, List.of(Genres.BOOK.getValue()));

        Listing[] listings = { listing_, listing_2, listing_3, listing_4 };

        for (Listing x : listings)
            listingRepository.insert(x);
        assertTrue(listingRepository.count() > 0);
    }

    @Test
    @DisplayName("Repository is looking for a username")
    void shouldFindByUsername() {
        // ARRANGE
        String user = "testBuddy";
        // ACT
        List<Listing> res = listingRepository.findByUsername(user);
        // ASSERT
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(user, res.get(0).getUsername());
    }

    @Test
    @DisplayName("Repository is looking for a username")
    void shouldFindByListingId() {
        // ARRANGE
        String listingId = "LARPING_3_ULTRA_LARPIST";
        // ACT
        Optional<Listing> res = listingRepository.findById(listingId);
        // ASSERT
        assertNotNull(res);
        assertEquals(listingId, res.get().getId());// result size
    }

    @Test
    @DisplayName("returns users owned Listings")
    void shouldFindByUserId() {
        // ARRANGE
        ObjectId comparable = compId;
        // ACT
        List<Listing> res = listingRepository.findByUserId(comparable);
        // ASSERT
        assertNotNull(res);
        assertTrue(res.size() > 0);
    }

    @Test
    @DisplayName("returns Listings by Status")
    void shouldFindByStatus() {
        // ARRANGE
        ListingStatus status = ListingStatus.AVAILABLE;
        // ACT
        List<Listing> res = listingRepository.findByStatus(status);
        // ASSERT
        assertNotNull(res);
        assertTrue(res.size() > 0);
    }

    @Test
    @DisplayName("returns Listings by ItemType")
    void shouldFindByItemType() {
        // ARRANGE
        ItemType type = ItemType.FullBoardGame;
        // ACT
        List<Listing> res = listingRepository.findByItemType(type.getValue());
        // ASSERT
        assertNotNull(res);
        assertTrue(res.size() > 0);
    }

}