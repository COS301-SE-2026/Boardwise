
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

import software.amazon.awssdk.services.s3.S3Client;

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
    void shouldCreateSaleListingWithDefaultImage() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        String fakeUser = "testBuddy";
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
    void shouldCreateRentalListingWithDefaultImage() {
        // ARRANGE
        String fakeToken = "this is a fake token";
        String fakeUser = "testBuddy";
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
    void shouldThrowWhenDeletingNonExistentListing(){
        // ARRANGE
        String fakeToken = "fake-token";
        when(jwtService.extractUserId(fakeToken)).thenReturn(new ObjectId());
        when(listingRepository.findById("bad-id")).thenReturn(Optional.empty());

        //ACT AND ASSERT
        assertThrows(IllegalArgumentException.class,
            () -> listingService.deleteListing("bad-id", fakeToken));
    }

    @BeforeTestMethod
    void addSaleListingToRepository(){
        listingRepository.insert(
            new Listing("fakeListingId", "IamReal", new ObjectId(), "full boardgame", "sale", 758.0, "Pretoria",
            true, "Board game of Ludo", "fair", "Ludo","original","fake Ludo description", "/images/fakeImage.jpeg",
            ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),List.of(Genres.BOOK.getValue(),Genres.AMERICAN_WEST.getValue()), null));
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

}