
package com.boardwise.backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.Condition;
import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.model.RentalPeriod;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

import software.amazon.awssdk.services.s3.S3Client;

@DisplayName("Listing Service Tests")
@ExtendWith(MockitoExtension.class) // auto create/inject mocks
class ListingServiceTest {
    
    private final String defaultIMG = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/default-listing-images/default.png";
    @Mock
    private ListingRepository listingRepository; // fake version of ListingRepository

    @Mock 
    private BoardGameRepository boardGameRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private JWTService jwtService;

    @Mock 
    private S3Client s3Client;

    @InjectMocks
    private ListingService listingService; // instance of Listing Service

    @Mock 
    private UserRepository userRepository; // fake version of UserRepository

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listingService, "publicUrl", "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/");
        ReflectionTestUtils.setField(listingService, "listingsBucket", "test-bucket");
        User p = new User();
        p.setUsername("testBuddy");
        lenient().when(userRepository.findById(any())).thenReturn(Optional.of(p));
    }

    @Test
    @DisplayName("Should create a valid sale listing (Assume BoardGame is in Repository)")
    void shouldCreateSaleListingWithBoardGameInRepo() {
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
    250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"), null);

        Boardgame bg = new Boardgame(null,null,"Ludo",null,null,null,null,null);
        
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);
        when(boardGameRepository.findByTitle("Ludo")).thenReturn(Optional.of(bg));

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
        assertEquals("original", res.version());
        assertNotNull(res.imageUrl());
        assertEquals("full boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertNull(res.rentalPeriod());
        verify(listingRepository, times(2)).save(any(Listing.class)); 

        verify(boardGameRepository, times(1)).findByTitle("Ludo");
        verify(boardGameRepository, never()).insert(any(Boardgame.class));
    }

        @Test
    @DisplayName("Should create a valid sale listing (Assume BoardGame is not in Repository)")
    void shouldCreateSaleListingWithBoardGameNotInRepo() {
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
    250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"), null);

        Boardgame bg = new Boardgame(null,null,"Ludo",null,null,null,null,null);
        
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);
        when(boardGameRepository.findByTitle("Ludo")).thenReturn(Optional.empty());

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
        assertEquals("original", res.version());
        assertNotNull(res.imageUrl());
        assertEquals("full boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertNull(res.rentalPeriod());
        verify(listingRepository, times(2)).save(any(Listing.class)); 

        verify(boardGameRepository, times(1)).insert(any(Boardgame.class));
        verify(boardGameRepository, times(1)).findByTitle("Ludo");
    }

    @Test
    @DisplayName("Should create a valid rental listing (Assume BoardGame is in Repository)")
    void shouldCreateRentalListingWithBoardGameInRepo() {
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


        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",50.0, "Ludo", "Pretoria", false,"lowkey doesn't exist","original", "like new", "have you played ludo before?", List.of("adventure", "strategy", "negotiation"),List.of("2030-05-31", "2030-06-01"));
                

        Boardgame bg = new Boardgame(null,null,"Ludo",null,null,null,null,null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, mockMultipartFile);

        // ASSERT
        assertNotNull(res);
        assertNotNull(res.gameTitle());
        assertEquals("Ludo", res.gameTitle());
        assertEquals(fakeUser, res.username());
        assertEquals("like new", res.condition());
        assertEquals("rental", res.listingType());
        assertTrue(res.genres().size() > 0);
        assertEquals(List.of("adventure", "strategy", "negotiation"), res.genres());
        assertEquals(50, res.price());
        assertEquals("Pretoria", res.location());
        assertEquals("original", res.version());
        assertEquals("full boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertTrue(res.rentalPeriod().getEndDate().compareTo(res.rentalPeriod().getStartDate()) > 0);
        assertEquals("2030-05-31", res.rentalPeriod().getStartDate().toString());
        assertEquals("2030-06-01", res.rentalPeriod().getEndDate().toString());
        verify(listingRepository, times(2)).save(any(Listing.class));
        assertNotNull(res.imageUrl());
        verify(boardGameRepository, times(1)).findByTitle("Ludo");
        verify(boardGameRepository, never()).insert(bg);
    }
    
    @Test
    @DisplayName("Should create a valid sale listing with the default image")
    void shouldCreateSaleListingWithDefaultImage() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = null;
        
        Listing fakeSavedListing = new Listing("fakeId", "testBuddy", new ObjectId(), 
        "full boardgame", "sale", 250,"Pretoria", true, "this is a fake title", "like new", "Ludo", "original", 
        "have you played ludo before?", null, ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy", "negotiation"), null);

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
    250, "Ludo", "Pretoria", true, null,"original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"), null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, mockMultipartFile);

        // ASSERT
        assertEquals(defaultIMG,res.imageUrl());
    }

    @Test
        @DisplayName("Should create a valid rental listing with the default image")

    void shouldCreateRentalListingWithDefaultImage() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = null;
        
        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));
               
        Listing fakeSavedListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "rental", 50,
        "Pretoria", false, "this is a fake title","like new", "Ludo", "original", "have you played ludo before?", null,
        ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy", "negotiation"), fakeRentalPeriod);


        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",50.0, "Ludo", "Pretoria", false,"lowkey doesn't exist","original", "like new", "have you played ludo before?", List.of("adventure", "strategy", "negotiation"),List.of("2030-05-31", "2030-06-01"));
                

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(listingRepository.save(any(Listing.class))).thenReturn(fakeSavedListing);

        // ACT
        ListingResponse res = listingService.createListing(listingRequest, fakeToken, mockMultipartFile);

        // ASSERT
        assertEquals(defaultIMG,res.imageUrl());
    }
    
    @Test
    @DisplayName("Should throw an illegal argument exception for an invalid starting date (date before today)")
    void shouldThrowIllegalArgumentExceptionForInvalidStartDate(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2010-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),List.of(fakeRentalPeriod.getStartDate().toString(),fakeRentalPeriod.getEndDate().toString()));

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test
    @DisplayName("Should throw an illegal argument exception for an invalid start date (start date after end date)")
    void shouldThrowIllegalArgumentExceptionForStartDateAfterEndDate(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2040-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),List.of(fakeRentalPeriod.getStartDate().toString(),fakeRentalPeriod.getEndDate().toString()));

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test
    @DisplayName("Should throw an illegal argument exception for an invalid end date (date before start date)")
    void shouldThrowIllegalArgumentExceptionForEndDateBeforeStartDate(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2026-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),List.of(fakeRentalPeriod.getStartDate().toString(),fakeRentalPeriod.getEndDate().toString()));

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test
    @DisplayName("Should throw an illegal argument exception for an Empty Rental Period for a rental listing)")
    void shouldThrowIllegalArgumentExceptionForEmptyRentalPeriod(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),null);

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test
    @DisplayName("Should throw an illegal argument exception for too many arguments in Date array")
    void shouldThrowIllegalArgumentExceptionForLargerDateArray(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),List.of(fakeRentalPeriod.getStartDate().toString(),fakeRentalPeriod.getEndDate().toString(),"2015-02-20"));
        
        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test
    @DisplayName("Should throw an illegal argument exception for: invalid listing type")
    void shouldThrowIllegalArgumentExceptionForInvalidListingType(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "borrow", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),List.of(fakeRentalPeriod.getStartDate().toString(),fakeRentalPeriod.getEndDate().toString()));

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test
    @DisplayName("Should throw an illegal argument exception for: Empty Description")
    void shouldThrowIllegalArgumentExceptionForEmptyDescription(){
        
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "", 
        List.of("adventure", "strategy", "negotiation"),null);

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }
    
    @Test
    @DisplayName("Should throw an illegal argument exception for: invalid amount")
    void shouldThrowIllegalArgumentExceptionForInvalidAmount(){
        
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2010-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
        -250, "Ludo", "Pretoria", true, "test.png","original", "like new", "ludo game title i guess", 
        List.of("adventure", "strategy", "negotiation"),null);

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }
    
    @Test
    @DisplayName("Should throw an illegal argument exception for: invalid condition")
    void shouldThrowIllegalArgumentExceptionForInvalidCondition(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2010-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "likenew", "AAAA", 
        List.of("adventure", "strategy", "negotiation"),null);

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }
    
    @Test
    @DisplayName("Should throw an illegal argument exception for: invalid genre")
    void shouldThrowIllegalArgumentExceptionForInvalidGenre(){
        
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2010-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2030-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "title", 
        List.of("fakeGenre", "strategy", "negotiation"),null);

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }
    
    @Test
    @DisplayName("Should throw an illegal argument exception for: invalid end date")
    void shouldThrowIllegalArgumentExceptionForInvalidEndDate(){
        //ARRANGE 
        String fakeToken = "this is a fake token";
        //Mocking image (Multipart File)
        MockMultipartFile mockMultipartFile = new MockMultipartFile( "image", "test.png","image/png", new byte[]{1,2,3});
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());

        //Rental Period 
        RentalPeriod fakeRentalPeriod= new RentalPeriod();
        fakeRentalPeriod.setStartDate(LocalDate.parse("2030-05-31"));
        fakeRentalPeriod.setEndDate(LocalDate.parse("2010-06-01"));

        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"),List.of(fakeRentalPeriod.getStartDate().toString(),fakeRentalPeriod.getEndDate().toString()));

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,()->listingService.createListing(listingRequest, fakeToken, mockMultipartFile));
    }

    @Test 
    @DisplayName("Should delete a valid listing")
    void shouldDeleteListing(){
        // ARRANGE
        String fakeToken = "Fake-token";
        String listingId = "listing-123";
        ObjectId userId = new ObjectId();
    
        Listing fakeListing = new Listing(listingId, "testBuddy", userId, "full boardgame", "sale", 100, "Pretoria", false, "A title", 
        "like new", "ludo", "version", "description", "fakeimage.png", 
        ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(), 
        List.of(Genres.ADVENTURE.getValue(),Genres.ANCIENT.getValue(), Genres.EDUCATIONAL.getValue()),null);
        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(fakeListing));
        // ACT
        listingService.deleteListing(listingId, fakeToken);
        // ASSERT
        verify(listingRepository, times(1)).deleteById(listingId);
    }

    @Test 
    @DisplayName("Should throw an forbidden argument exception for: listing not owned by you")
    void shouldThrowForbiddenWhenDeletingListingYouDontOwn(){
        //ARRANGE
        String fakeToken = "fake-token";
        String listingId = "listing-123";

        Listing fakeListing = new Listing(listingId, "otherUser", new ObjectId(), "full boardgame", "sale", 100, "Pretoria", false, "A title","like new", "Ludo", "original", "description", null, 
        ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),List.of("strategy"), null);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(fakeListing));

        //ACT AND ASSERT
        assertThrows(ForbiddenException.class,
        ()-> listingService.deleteListing(listingId,fakeToken));

        verify(listingRepository, times(0)).deleteById(any());
    }

    @Test
     @DisplayName("Should throw an Illegal argument exception for: Non-Existent Listing")
    void shouldThrowWhenDeletingNonExistentListing(){
        // ARRANGE
        String fakeToken = "fake-token";
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(listingRepository.findById("bad-id")).thenReturn(Optional.empty());

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,
            () -> listingService.deleteListing("bad-id", fakeToken));
    }

    @Test 
    @DisplayName("Should Edit a listing with Game in repository")
    void shouldEditListingWithGameInRepository(){
        // ARRANGE
        String fakeToken = "fake-Token";
        String listingId = "fakeistingID";
        ObjectId  userId = new ObjectId();
        Boardgame bg = new Boardgame(null,null,"Ludo",null,null,null,null,null);

        Listing existingListing = new Listing(listingId, "testBuddy", userId, "full boardgame", "sale", 100,
        "Pretoria", false, "Old title", "like new", "Ludo", "original",
        "old description", "fakeimage.png", ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "New title",
        300, "Ludo", "Pretoria", false, "test.png", "original", "like new",
        "updated description", List.of("adventure", "strategy"), null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(existingListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(boardGameRepository.findByTitle(anyString())).thenReturn(Optional.of(bg));

        MockMultipartFile mockImg = new MockMultipartFile("image", "newImage.jpg", "image/jpeg", new byte[]{1, 2, 3});


        // ACT
        ListingResponse res = listingService.updateListing(listingId, listingRequest, fakeToken, mockImg);
        // ASSERT
        assertNotNull(res);
        assertEquals(300, res.price());
        assertEquals("updated description", res.description());
        assertEquals("New title", res.listingTitle());
        verify(listingRepository, times(1)).save(any(Listing.class));  

        verify(boardGameRepository, times(1)).findByTitle("Ludo");
        verify(boardGameRepository, never()).insert(any(Boardgame.class));    
    }

    @Test 
    @DisplayName("Should Edit a listing with Game not in repository")
    void shouldEditListingWithGameNotInRepository(){
        // ARRANGE
        String fakeToken = "fake-Token";
        String listingId = "fakeistingID";
        ObjectId  userId = new ObjectId();
        Boardgame bg = new Boardgame(null,null,"Ludo",null,null,null,null,null);

        Listing existingListing = new Listing(listingId, "testBuddy", userId, "full boardgame", "sale", 100,
        "Pretoria", false, "Old title", "like new", "Ludo", "original",
        "old description", "fakeimage.png", ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "New title",
        300, "Ludo", "Pretoria", false, "test.png", "original", "like new",
        "updated description", List.of("adventure", "strategy"), null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(existingListing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(boardGameRepository.findByTitle(anyString())).thenReturn(Optional.empty());

        MockMultipartFile mockImg = new MockMultipartFile("image", "newImage.jpg", "image/jpeg", new byte[]{1, 2, 3});


        // ACT
        ListingResponse res = listingService.updateListing(listingId, listingRequest, fakeToken, mockImg);
        // ASSERT
        assertNotNull(res);
        assertEquals(300, res.price());
        assertEquals("updated description", res.description());
        assertEquals("New title", res.listingTitle());
        verify(listingRepository, times(1)).save(any(Listing.class));  


        verify(boardGameRepository, times(1)).insert(any(Boardgame.class));
        verify(boardGameRepository, times(1)).findByTitle("Ludo");
    }

    @Test   
    @DisplayName("Edit Listing should throw Forbbiden when updating a listing you do not own")
    void shouldThrowForbiddenWhenUpdatingListingYouDontOwn(){

        ObjectId  id = new ObjectId();
        //ARRANGE
        Listing existingListing = new Listing("fakeId", "testBuddy", id, "full boardgame", "sale", 100,
                "Pretoria", false, "Old title", "like new", "Ludo", "original",
                "old description", "fakeimage.png", ListingStatus.AVAILABLE,
                LocalDateTime.now(), LocalDateTime.now(),
                List.of("adventure", "strategy"), null);

        ObjectId altId = new ObjectId();

        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "AAAA", 
        List.of("adventure", "strategy", "negotiation"),null);
        when(listingRepository.findById("fakeId")).thenReturn(Optional.of(existingListing)); 
        when(jwtService.extractUserId("fakeToken")).thenReturn(altId);
        
        //ACT & ASSERT 
        assertThrows(ForbiddenException.class, ()->{
            listingService.updateListing("fakeId", listingRequest, "fakeToken", null);
        });
    }

    @Test
    @DisplayName("Edit Listing should throw on updating a nonexistent ")
    void shouldThrowWhenUpdatingNonExistentListing(){
        //ARRANGE 
        ListingRequest listingRequest = new ListingRequest("full boardgame", "sale", "something something something",
        250, "Ludo", "Pretoria", true, "test.png","original", "like new", "AAAA", 

        List.of("adventure", "strategy", "negotiation"),null);
        when(listingRepository.findById("fakeId")).thenReturn(Optional.empty()); 
        
        //ACT & ASSERT 
        assertThrows(IllegalArgumentException.class, ()->{
            listingService.updateListing("fakeId", listingRequest, "fakeToken", null);
        });
    }

    @Test
    @DisplayName("Edit Listing should throw an IllegalArgumentException on invalid ItemType")
    void shouldThrowForInvalidItemTypeOnUpdate (){
        //ARRANGE
        String fakeToken = "fake-token";
        String fakeUsername = "testBuddy";
        String listingId = "listingId";
        ObjectId userId = new ObjectId();
        //RentalPeriod 
        RentalPeriod rp = new RentalPeriod();
        rp.setStartDate(LocalDate.now());
        rp.setEndDate(LocalDate.now().plusDays(60));
        Listing cmpListing = new Listing(listingId, fakeUsername, userId,"assets", "rental", 3210, "pretoria",
         false,"MONOPOLY: whole bunch of nonsense", "fair","Monopoly", "Original","some monopoly",defaultIMG,
         ListingStatus.AVAILABLE, LocalDateTime.now(),  LocalDateTime.now(), List.of(Genres.GAME_SYSTEM.getValue()),rp);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        ListingRequest req = new ListingRequest("invalid", "rental", "MONOPOLY: whole bunch of nonsense", 3210, "Monopoly", "pretoria", 
        false,defaultIMG, "Original","fair", "some monopoly",  
        List.of(Genres.GAME_SYSTEM.getValue()),List.of(rp.getStartDate().toString(),rp.getEndDate().toString()));

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(cmpListing));
        //ACT && ASSERT 
        assertThrows(IllegalArgumentException.class, ()->{
            listingService.updateListing(listingId, req, fakeToken, null);
        });
    }

    @Test
    @DisplayName("Edit Listing should throw an IllegalArgumentException on invalid Genres")
    void shouldThrowForInvalidGenreOnUpdate(){
        //ARRANGE
        String fakeToken = "fake-token";
        String fakeUsername = "testBuddy";
        String listingId = "listingId";
        ObjectId userId = new ObjectId();
        //RentalPeriod 
        RentalPeriod rp = new RentalPeriod();
        rp.setStartDate(LocalDate.now());
        rp.setEndDate(LocalDate.now().plusDays(60));
        Listing cmpListing = new Listing(listingId, fakeUsername, userId,"assets", "rental", 3210, "pretoria",
         false,"MONOPOLY: whole bunch of nonsense", "fair","Monopoly", "Original","some monopoly",defaultIMG,
         ListingStatus.AVAILABLE, LocalDateTime.now(),  LocalDateTime.now(), List.of(Genres.GAME_SYSTEM.getValue()),rp);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        ListingRequest req = new ListingRequest("assets", "rental", "MONOPOLY: whole bunch of nonsense", 3210, "Monopoly", "pretoria", 
        false,defaultIMG, "Original","fair", "some monopoly",  
        List.of("Fake Genre"),List.of(rp.getStartDate().toString(),rp.getEndDate().toString()));

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(cmpListing));
        //ACT && ASSERT 
        assertThrows(IllegalArgumentException.class, ()->{
            listingService.updateListing(listingId, req, fakeToken, null);
        });
    }

    @Test
    @DisplayName("Edit Listing should throw if time of rental start is before today")
    void shouldThrowWhenUpdateRentalPeriodHasStartDateInPast(){
        //ARRANGE
        String fakeToken = "fake-token";
        String fakeUsername = "testBuddy";
        String listingId = "listingId";
        ObjectId userId = new ObjectId();
        //RentalPeriod 
        RentalPeriod rp = new RentalPeriod();
        rp.setStartDate(LocalDate.now());
        rp.setEndDate(LocalDate.now().plusDays(60));
        Listing cmpListing = new Listing(listingId, fakeUsername, userId,"assets", "rental", 3210, "pretoria",
         false,"MONOPOLY: whole bunch of nonsense", "fair","Monopoly", "Original","some monopoly",defaultIMG,
         ListingStatus.AVAILABLE, LocalDateTime.now(),  LocalDateTime.now(), List.of(Genres.GAME_SYSTEM.getValue()),rp);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        ListingRequest req = new ListingRequest("assets", "rental", "MONOPOLY: whole bunch of nonsense", 3210, "Monopoly", "pretoria", 
        false,defaultIMG, "Original","fair", "some monopoly",  
        List.of(Genres.GAME_SYSTEM.getValue()),List.of(LocalDate.now().minusDays(55).toString(),LocalDate.now().plusDays(81).toString()));

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(cmpListing));
        //ACT && ASSERT 
        assertThrows(IllegalArgumentException.class, ()->{
            listingService.updateListing(listingId, req, fakeToken, null);
        });
    }

    @Test
    @DisplayName("Edit listings should throw an Illegal argument Exception")
    void shouldThrowWhenUpdateRentalEndBeforeStart (){
        //ARRANGE
        String fakeToken = "fake-token";
        String fakeUsername = "testBuddy";
        String listingId = "listingId";
        ObjectId userId = new ObjectId();
        //RentalPeriod 
        RentalPeriod rp = new RentalPeriod();
        rp.setStartDate(LocalDate.now());
        rp.setEndDate(LocalDate.now().plusDays(60));
        Listing cmpListing = new Listing(listingId, fakeUsername, userId,"assets", "rental", 3210, "pretoria",
         false,"MONOPOLY: whole bunch of nonsense", "fair","Monopoly", "Original","some monopoly",defaultIMG,
         ListingStatus.AVAILABLE, LocalDateTime.now(),  LocalDateTime.now(), List.of(Genres.GAME_SYSTEM.getValue()),rp);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        ListingRequest req = new ListingRequest("assets", "rental", "MONOPOLY: whole bunch of nonsense", 3210, "Monopoly", "pretoria", 
        false,defaultIMG, "Original","fair", "some monopoly",  
        List.of(Genres.GAME_SYSTEM.getValue()),List.of(rp.getStartDate().toString(),rp.getStartDate().minusDays(5).toString()));

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(cmpListing));
        //ACT && ASSERT 
        assertThrows(IllegalArgumentException.class, ()->{
            listingService.updateListing(listingId, req, fakeToken, null);
        });
    }

    @Test
    @DisplayName("Edit Listing must successfully edit only the image nothing more")
    void shouldUpdateImageWhenValidFileProvided(){
        //ARRANGE
        String fakeToken = "fake-token";
        String fakeUsername = "testBuddy";
        String listingId = "listingId";
        ObjectId userId = new ObjectId();
        String newImageUrl = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/listings/listingId/newImage.jpg";
        
        //RentalPeriod 
        RentalPeriod rp = new RentalPeriod();
        rp.setStartDate(LocalDate.now());
        rp.setEndDate(LocalDate.now().plusDays(60));

        Listing cmpListing = new Listing(listingId, fakeUsername, userId,"assets", "rental", 3210, "pretoria",
         false,"MONOPOLY: whole bunch of nonsense", "fair","Monopoly", "Original","some monopoly",defaultIMG,
         ListingStatus.AVAILABLE, LocalDateTime.now(),  LocalDateTime.now(), List.of(Genres.GAME_SYSTEM.getValue()),rp);

        when(jwtService.extractUserId(fakeToken)).thenReturn(userId);
        
        MockMultipartFile mockImg = new MockMultipartFile("image", "newImage.jpg", "image/jpeg", new byte[]{1, 2, 3});

    
        ListingRequest req = new ListingRequest("assets", "rental", "MONOPOLY: whole bunch of nonsense", 3210, "Monopoly", "pretoria",
        false, defaultIMG, "Original", "fair", "some monopoly", 
        List.of(Genres.GAME_SYSTEM.getValue()), List.of(rp.getStartDate().toString(), rp.getEndDate().toString()));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation->invocation.getArgument(0));
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(cmpListing));
        
        ListingService spyWithMockito = Mockito.spy(listingService);

        try {
            doReturn(newImageUrl).when(spyWithMockito).uploadImageToR2(eq(listingId), any(MultipartFile.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        //ACT 
        ListingResponse res = spyWithMockito.updateListing(listingId, req, fakeToken, mockImg);
        ReflectionTestUtils.setField(spyWithMockito, "publicUrl", "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/");
        ReflectionTestUtils.setField(spyWithMockito, "listingsBucket", "test-bucket");

        //ASSERT 
        assertNotNull(res);
        assertEquals(newImageUrl, res.imageUrl());
        verify(listingRepository,times(1)).save(any(Listing.class));
    } 

    @Test
    @DisplayName("Edit Listing should throw if updating image is null")
    void shouldReplaceImageWhenImageFilenameIsNull(){
        //ARRANGE
        String listingId = "someListingId";
        String fakeUsername= "testBuddy";
        ObjectId  userId = new ObjectId();
        String token = "fake-token";
        //RentalPeriod 
        RentalPeriod rp = new RentalPeriod();
        rp.setStartDate(LocalDate.now());
        rp.setEndDate(LocalDate.now().plusDays(60));
        Listing cmpListing = new Listing(listingId, fakeUsername, userId,"assets", "rental", 3210, "pretoria",
         false,"MONOPOLY: whole bunch of nonsense", "fair","Monopoly", "Original","some monopoly",defaultIMG,
         ListingStatus.AVAILABLE, LocalDateTime.now(),  LocalDateTime.now(), List.of(Genres.GAME_SYSTEM.getValue()),rp);

        ListingRequest req = new ListingRequest(token, listingId, listingId, cmpListing.getPrice(), cmpListing.getGameTitle(), cmpListing.getLocation(), cmpListing.getIsNegotiable(),null, cmpListing.getVersion(), cmpListing.getCondition(),
         cmpListing.getDescription(), cmpListing.getGenres(), List.of(cmpListing.getRentalPeriod().getStartDate().toString(),cmpListing.getRentalPeriod().getEndDate().toString()));
        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(cmpListing));
        
        //ACT & ASSERT 
        assertThrows(IllegalArgumentException.class, ()->listingService.updateListing(listingId, req, token, null));
        
    }


    @Test
    @DisplayName("Filter should return listings filtered by gameTitle")
    void shouldReturnListingFilteredByGameTitle(){
        //ARRANGE
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Monopoly", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);
        
        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class),eq(Listing.class))).thenReturn(List.of(fakeListing));

        //ACT
        Page<ListingResponse> res = listingService.getByFilter("Monopoly", null, null, null, null, null, null, null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertEquals("Monopoly", res.getContent().get(0).gameTitle());
    }

    @Test
    @DisplayName("Filter should return listings filtered by listingTitle")
    void shouldReturnListingFilteredByListingTitle(){
        //ARRANGE
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);
        
        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class),eq(Listing.class))).thenReturn(List.of(fakeListing));

        //ACT
        Page<ListingResponse> res = listingService.getByFilter(null, "fake", null, null, null, null, null, null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertTrue(res.getContent().get(0).listingTitle().contains("fake"));
    }

    @Test
    @DisplayName("Filter should return listings filtered by condition")
    void shouldReturnListingFilteredByCondition(){
        //ARRANGE
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "fair", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);
        
        RentalPeriod rp  = new RentalPeriod();
        rp.setStartDate(LocalDate.now().plusDays(50));
        rp.setEndDate(LocalDate.now().plusDays(51));

        Listing fakeListing_1 = new Listing("fakeId_2", "sumwon", new ObjectId(), "partial boardgame", "rental", 3850,
        "Pretoria", true, "titles dont have to be unique", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"),rp);
        
        Listing fakeListing_2 = new Listing("fakeId_3", "sumwonElse", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);
        
        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(3L);
        when(mongoTemplate.find(any(Query.class),eq(Listing.class))).thenReturn(List.of(fakeListing,fakeListing_1, fakeListing_2));

        //ACT
        Page<ListingResponse> res = listingService.getByFilter(null,null, null, null, null, null,  List.of("fair","like new") ,null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(3, res.getTotalElements());
    }
  
    @Test
    @DisplayName("Filter should return listings filtered by price range")
    void shouldReturnListingsFilteredByPriceRange(){
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 150,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Listing.class))).thenReturn(List.of(fakeListing));
        
        //ACT
        Page<ListingResponse> res = listingService.getByFilter(null, null, null, null, 100.0, 200.0, null, null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertTrue(res.getContent().get(0).price() >= 100.0 && res.getContent().get(0).price() <= 200.0);
    }

    @Test
    @DisplayName("Filter should return listings filtered by minPrice only")
    void shouldReturnListingsFilteredByMinPrice(){
        //ARRANGE
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 550,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Listing.class))).thenReturn(List.of(fakeListing));

        //ACT
        Page<ListingResponse> res = listingService.getByFilter(null, null, null, null, 300.0, null, null, null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertTrue(res.getContent().get(0).price() >= 300.0);
    }

    @DisplayName("Filter should return listings filtered by maxPrice only")
    void shouldReturnListingsFilteredByMaxPrice(){
        //ARRANGE
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Listing.class))).thenReturn(List.of(fakeListing));

        //ACT
        Page<ListingResponse> res = listingService.getByFilter(null, null, null, null, null ,500.0, null, null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        assertTrue(res.getContent().get(0).price() <= 500.0);
    }

    @Test
    @DisplayName("Should return paginated listings")
    void shouldReturnPaginatedListings(){
        //ARRANGE
        Listing l1 = new Listing("id1", "testBuddy", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", false, "title1", "like new", "Ludo", "original",
        "desc", defaultIMG, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(), List.of(Genres.ADVENTURE.getValue()), null);

        Listing l2 = new Listing("id2", "testBuddy", new ObjectId(), "full boardgame", "sale", 300,
        "Pretoria", false, "title2", "like new", "Chess", "original",
        "desc", defaultIMG, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(), List.of(Genres.ADVENTURE.getValue()), null);

        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(2L);
        when(mongoTemplate.find(any(Query.class), eq(Listing.class))).thenReturn(List.of(l1, l2));
        
        //ACT
        Page<ListingResponse> res = listingService.getByFilter(null, null, null, null, null, null, null, null, 1, 2);

        //ASSERT
        assertNotNull(res);
        assertEquals(2, res.getTotalElements());
        assertEquals(2, res.getContent().size());
    }

    @Test 
    @DisplayName("Filter should return empty page when no listings match filter")
    void shouldReturnEmptyPageWithNoMatch(){
        //ARRANGE
        when(mongoTemplate.count(any(Query.class), eq(Listing.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Listing.class))).thenReturn(List.of());

        //ACT
        Page<ListingResponse> res = listingService.getByFilter("NonExistentGame", null, null, null, null, null, null, null, null, null);

        //ASSERT
        assertNotNull(res);
        assertEquals(0, res.getTotalElements());
        assertTrue(res.getContent().isEmpty());
    }

    @Test
    @DisplayName("Should get All Listings")
    void shouldGetEveryListing(){
        // ARRANGE
        Listing fakeListing = new Listing("fakeId", "testBuddy", new ObjectId(), "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        Listing fakeListing_1 = new Listing("fakeId_2", "SomeoneElse", new ObjectId(), "partial boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        Listing fakeListing_2 = new Listing("fakeId_3", "anotherPerson", new ObjectId(), "pieces", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        when(listingRepository.findByStatus(ListingStatus.AVAILABLE)).thenReturn(List.of(fakeListing, fakeListing_1, fakeListing_2));


        // ACT
        List<ListingResponse> res = listingService.getAllActiveListings();

        // ASSERT
        assertNotNull(res);
        assertTrue(res.size()>0);
        assertTrue(res.size() == 3);
    }

    @Test
    @DisplayName("Should get Users Listings")
    void shouldGetUsersListings(){
        // ARRANGE
        String fakeToken = "fake-Token";
        ObjectId fakeUserId = new ObjectId();

        Listing fakeListing = new Listing("fakeId", "testBuddy", fakeUserId, "full boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        Listing fakeListing_1 = new Listing("fakeId", "testBuddy", fakeUserId, "partial boardgame", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        Listing fakeListing_2 = new Listing("fakeId", "testBuddy", fakeUserId, "pieces", "sale", 250,
        "Pretoria", true, "fake title", "like new", "Ludo", "original",
        "have you played ludo before?", null, ListingStatus.AVAILABLE,
        LocalDateTime.now(), LocalDateTime.now(),
        List.of("adventure", "strategy"), null);

        when (jwtService.extractUserId(fakeToken)).thenReturn(fakeUserId);
        when(listingRepository.findByUserId(fakeUserId)).thenReturn(List.of(fakeListing, fakeListing_1, fakeListing_2));
        
        // ACT
        List<ListingResponse> res = listingService.getUserListings(fakeToken);

        // ASSERT
        assertNotNull(res);
        assertTrue(res.size()>0);
        assertEquals(3,res.size());
    }

    @Test 
    @DisplayName("Should get No listings : User does not have any listing")
    void shouldZeroUserListings(){
        //ARRANGE 
        String fakeToken = "fake-Token";
        ObjectId fakeUserId = new ObjectId();

        when(jwtService.extractUserId(fakeToken)).thenReturn(fakeUserId);
        when(listingRepository.findByUserId(fakeUserId)).thenReturn(List.of());
        
        //ACT
        List<ListingResponse> res = listingService.getUserListings(fakeToken);

        //ASSERT 
        assertNotNull(res);
        assertEquals(0,res.size());
    }

    @Test
    @DisplayName("Should return the Listing") 
    void shouldGetListingById(){
        // ARRANGE
        String listingId = "fake";
        Listing fakeListing = new Listing(listingId, "fakeUsername", new ObjectId(), "pieces", "sale", 4289,
    "Pretoria", true, "fakeTitle", Condition.FAIR.getValue(), "Ludo", "Original",
    "fake description", "", ListingStatus.AVAILABLE,
            LocalDateTime.now(), LocalDateTime.now(),
            List.of(Genres.ANCIENT.getValue()), null);
                    
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(fakeListing));
        //ACT 
        ListingResponse res = listingService.getListingById(listingId);

        // ASSERT 
        assertNotNull(res);
        assertEquals("fakeTitle", res.listingTitle());
        assertEquals("Ludo", res.gameTitle());
    }

    @Test
    @DisplayName("Should throw an Exception for: Non existent Id")
    void shouldThrowWhenIdDoesNotExist(){
        //ARRANGE 

        String listingId = "fake";

        when(listingRepository.findById(listingId)).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThrows(IllegalArgumentException.class,
        ()-> listingService.getListingById(listingId));
    }

    
}