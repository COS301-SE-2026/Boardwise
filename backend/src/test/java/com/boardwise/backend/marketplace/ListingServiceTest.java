package com.boardwise.backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.model.RentalPeriod;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.shared.security.JWTService;

import jakarta.validation.constraints.AssertTrue;
import software.amazon.awssdk.services.s3.S3Client;

@ExtendWith(MockitoExtension.class) // auto create/inject mocks
class ListingServiceTest {
    @Mock
    private ListingRepository listingRepository; // fake version of ListingRepository

    @Mock
    private JWTService jwtService;

    @Mock 
    private S3Client s3Client;

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
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        
        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));
               
        Listing fakeSavedListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "rental", 50,
        "Pretoria", false, "this is a fake title","like new", "Ludo", "original", "have you played ludo before?", null,
        ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy", "negotiation"), fakeRentalPeriod);


        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",50, "Ludo", "Pretoria", false,
                "original", "like new", "have you played ludo before?", List.of("adventure", "strategy", "negotiation"),List.of("2030-05-31", "2030-06-01"));
                

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(jwtService.extractUsername(fakeToken)).thenReturn(fakeUser);
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, mockMultipartFile);

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
        assertEquals("full boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertEquals("2030-05-31", res.rentalPeriod().getStartDate().toString());
        assertEquals("2030-06-01", res.rentalPeriod().getEndDate().toString());
        verify(listingRepository, times(2)).save(any(Listing.class));

    }

    @Test
    void shouldCreateSaleListing() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        String fakeUser = "testBuddy";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        
        Listing fakeSavedListing = new Listing("fakeId", "testBuddy", new ObjectId(), 
        "full boardgame", "sale", 250,"Pretoria", true, "this is a fake title", "like new", "Ludo", "original", 
        "have you played ludo before?", null, ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy", "negotiation"), null);

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
    250, "Ludo", "Pretoria", true, "original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"), null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(jwtService.extractUsername(fakeToken)).thenReturn(fakeUser);
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, mockMultipartFile);

        // ASSERT
        assertNotNull(res);
        assertNotNull(res.gameTitle());
        assertEquals("Ludo", res.gameTitle());
        assertNotNull(res.username());
        assertEquals(fakeUser, res.username());
        assertNotNull(res.condition());
        assertEquals("like new", res.condition());
        assertNotNull(res.listingType());
        assertEquals("sale", res.listingType());
        assertNotNull(res.isNegotiable());
        assertTrue(res.isNegotiable());
        assertNotNull(res.genres());
        assertEquals(List.of("adventure", "strategy", "negotiation"), res.genres());
        assertTrue(res.price()>0);
        assertEquals(250, res.price());
        assertNotNull(res.location());
        assertEquals("Pretoria", res.location());
        assertNotNull(res.version());
        assertEquals("original", res.version());
        assertNotNull(res.itemType());
        assertEquals("full boardgame", res.itemType());
        assertNotNull(res.description());
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