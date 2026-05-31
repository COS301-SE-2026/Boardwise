package com.boardwise.backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.shared.security.JWTService;

@ExtendWith(MockitoExtension.class) // auto create/inject mocks
class ListingServiceTest {
    @Mock
    private ListingRepository listingRepository; // fake version of ListingRepository

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private ListingService listingService; // instance of Listing Service

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldCreateRentalListing() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        String fakeUser = "testBuddy";
        Listing fakeSavedListing = new Listing("fakeId", "testBuddy", new ObjectId(), "boardgame", "rental", 50,
                "Pretoria", false, "like new", "Ludo", "original", "have you played ludo before?", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("adventure", "strategy", "negotiation"), null);

        ListingRequest listingRequest = new ListingRequest("boardgame", "rental", 50, "Ludo", "Pretoria", false,
                "original", "like new", "have you played ludo before?", List.of("adventure", "strategy", "negotiation"),
                List.of("2030-05-31", "2030-06-01"));
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(jwtService.extractUsername(fakeToken)).thenReturn(fakeUser);
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, null);

        // ASSERT
        assertNotNull(res);
        assertEquals("Ludo", res.gameTitle());
        assertEquals(fakeUser, res.username());
        assertEquals("like new", res.condition());
        assertEquals("rental", res.listingType());
        assertEquals(List.of("adventure", "strategy", "negotiation"), res.genres());
        assertEquals(50, res.price());
        assertEquals("Pretoria", res.location());
        assertEquals("original", res.version());
        assertEquals("boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertNotNull(res.rentalPeriod());
        assertEquals("2030-05-31", res.rentalPeriod().getStartDate().toString());
        assertEquals("2030-06-01", res.rentalPeriod().getEndDate().toString());

        verify(listingRepository, times(2)).save(any(Listing.class));

    }

    @Test
    void shouldCreateSaleListing() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        String fakeUser = "testBuddy";
        Listing fakeSavedListing = new Listing("fakeId", "testBuddy", new ObjectId(), "boardgame", "sale", 250,
                "Pretoria", true, "like new", "Ludo", "original", "have you played ludo before?", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("adventure", "strategy", "negotiation"), null);

        ListingRequest listingRequest = new ListingRequest("boardgame", "sale", 250, "Ludo", "Pretoria", false,
                "original", "like new", "have you played ludo before?", List.of("adventure", "strategy", "negotiation"),
                null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(jwtService.extractUsername(fakeToken)).thenReturn(fakeUser);
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, null);

        // ASSERT
        assertNotNull(res);
        assertEquals("Ludo", res.gameTitle());
        assertEquals(fakeUser, res.username());
        assertEquals("like new", res.condition());
        assertEquals("sale", res.listingType());
        assertTrue(res.isNegotiable());
        assertEquals(List.of("adventure", "strategy", "negotiation"), res.genres());
        assertEquals(250, res.price());
        assertEquals("Pretoria", res.location());
        assertEquals("original", res.version());
        assertEquals("boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertNull(res.rentalPeriod());

        verify(listingRepository, times(2)).save(any(Listing.class)); 
    }

    @Test 
    void shouldDeleteListing(){
        // ARRANGE
        // ACT
        // ASSERT
    }

    @Test 
    void shouldEditListing(){
        // ARRANGE
        // ACT
        // ASSERT
    }

    @Test
    void shouldGetEveryListing(){
        // ARRANGE
        // ACT
        // ASSERT
    }

    @Test
    void shouldGetUsersListings(){
        // ARRANGE
        // ACT
        // ASSERT
    }
}