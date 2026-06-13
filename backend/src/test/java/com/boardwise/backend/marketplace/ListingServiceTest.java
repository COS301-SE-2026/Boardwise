
package com.boardwise.backend.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.reactor.IOSession.Status;

import software.amazon.awssdk.services.s3.S3Client;

@DisplayName("Listing Service Tests")
@ExtendWith(MockitoExtension.class) // auto create/inject mocks
class ListingServiceTest {
    
    private final String defaultIMG = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/default-listing-images/default.png";
    @Mock
    private ListingRepository listingRepository; // fake version of ListingRepository

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
    @DisplayName("Should create a valid sale listing")
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
    250, "Ludo", "Pretoria", true, "test.png","original", "like new", "have you played ludo before?", 
        List.of("adventure", "strategy", "negotiation"), null);

        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        
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
        assertEquals("original", res.version());
        assertNotNull(res.imageUrl());
        assertEquals("full boardgame", res.itemType());
        assertEquals("have you played ludo before?", res.description());
        assertNull(res.rentalPeriod());
        verify(listingRepository, times(2)).save(any(Listing.class)); 
    }

    @Test
    @DisplayName("Should create a valid rental listing")
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


        ListingRequest listingRequest = new ListingRequest("full boardgame", "rental", "something something something",50.0, "Ludo", "Pretoria", false,"lowkey doesn't exist","original", "like new", "have you played ludo before?", List.of("adventure", "strategy", "negotiation"),List.of("2030-05-31", "2030-06-01"));
                

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
    @DisplayName("Should Edit a listing")
    void shouldEditListing(){
        // ARRANGE
        String fakeToken = "fake-Token";
        String listingId = "fakeistingID";
        ObjectId  userId = new ObjectId();

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

        // ACT
        ListingResponse res = listingService.updateListing(listingId, listingRequest, fakeToken, null);
        // ASSERT
        assertNotNull(res);
        assertEquals(300, res.price());
        assertEquals("updated description", res.description());
        assertEquals("New title", res.listingTitle());
        verify(listingRepository, times(1)).save(any(Listing.class));   
    }

    @Test   
    @DisplayName("Placeholder")
    void shouldThrowForbiddenWhenUpdatingListingYouDontOwn(){
        //ARRANGE
        //ACT 
        //ASSERT 
    }

    @Test
    @DisplayName("Placeholder")
    void shouldThrowWhenUpdatingNonExistentListing(){
        //ARRANGE
        //ACT 
        //ASSERT 
    }

    @Test
    @DisplayName("Placeholder")
    void shouldThrowForInvalidItemTypeOnUpdate (){
        //ARRANGE
        //ACT 
        //ASSERT 
    }

    @Test
    @DisplayName("Placeholder")
    void shouldThrowForInvalidGenreOnUpdate(){
        //ARRANGE
        //ACT 
        //ASSERT 
    }

    @Test
    @DisplayName("Placeholder")
    void shouldThrowWhenUpdateRentalPeriodHasStartDateInPast(){
        //ARRANGE
        //ACT 
        //ASSERT 
    }
    @Test
    @DisplayName("Placeholder")
    void shouldThrowWhenUpdateRentalEndBeforeStart (){
        //ARRANGE
        //ACT 
        //ASSERT 
    }

    @Test
    @DisplayName("Placeholder")
    void shouldUpdateImageWhenValidFileProvided(){
        //ARRANGE
        //ACT 
        //ASSERT 
    } 

    @Test
    @DisplayName("Placeholder")
    void shouldThrowWhenImageFilenameIsNull(){
        //ARRANGE
        //ACT 
        //ASSERT 
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