package com.boardwise.backend.marketplace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.marketplace.service.ListingService;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.repos.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListingServiceTest {

        @Mock
        private ListingRepository listingRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private JWTService jwtService;

        @InjectMocks
        private ListingService listingService;

        private final String test_username = "testUsername";
        private final String test_token = "test.jwt.token";
        private final String test_listing_id = "listing123";
        private final double test_price = 299.99;
        private final String test_description = "test description";
        private final List<String> test_genres = List.of("negotiation");

        private MockMultipartFile test_image;
        private Listing savedListing;

        @BeforeEach
        void setUp() {
                test_image = new MockMultipartFile(
                                "image", "test.jpg", "image/jpeg", "fake-image-bytes".getBytes());

                savedListing = new Listing(
                                test_listing_id, test_username, "boardgame", "sale",
                                test_price, "Catan", test_description, null,
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                test_genres, null);
        }

        // CREATE

        @Test
        void create_SALE_listing_valid_details() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", test_price, "Catan",
                                test_description, test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.save(any())).thenReturn(savedListing);

                ListingResponse response = listingService.createListing(req, test_token, test_image);

                assertNotNull(response);
                assertEquals("Catan", response.gameTitle());
                assertEquals("sale", response.listingType());
                assertEquals(test_price, response.price());
                assertEquals(test_username, response.username());
                assertEquals(ListingStatus.AVAILABLE, response.status());
                verify(listingRepository, times(1)).save(any());
        }

        @Test
        void getByFilter_excludes_sold_listings() {
                // ARRANGE
                Listing availableListing = new Listing(
                                "id1", test_username, "boardgame", "sale",
                                100.00, "Catan", test_description, null,
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                List.of("economic"), null);

                Listing soldListing = new Listing(
                                "id2", test_username, "boardgame", "sale",
                                200.00, "Monopoly", test_description, null,
                                ListingStatus.SOLD, LocalDateTime.now(), LocalDateTime.now(),
                                List.of("economic"), null);

                when(listingRepository.findAll()).thenReturn(List.of(availableListing, soldListing));

                // ACT
                List<ListingResponse> results = listingService.getByFilter(null, null, null, null, null);

                // ASSERT
                assertEquals(1, results.size());
                assertEquals("Catan", results.get(0).gameTitle());
        }

        @Test
        void create_RENTAL_listing_valid_details() {
                List<String> rentalPeriod = List.of("2026-06-01", "2026-06-07");
                ListingRequest req = new ListingRequest(
                                "boardgame", "rental", test_price, "Catan",
                                test_description, test_genres, rentalPeriod);

                Listing rentalListing = new Listing(
                                test_listing_id, test_username, "boardgame", "rental",
                                test_price, "Catan", test_description, null,
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                test_genres, null);

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.save(any())).thenReturn(rentalListing);

                ListingResponse response = listingService.createListing(req, test_token, test_image);

                assertNotNull(response);
                assertEquals("rental", response.listingType());
                verify(listingRepository, times(1)).save(any());
        }

        @Test
        void create_listing_negative_price_throws() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", -10.00, "Catan",
                                test_description, test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void create_listing_invalid_genre_throws() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", test_price, "Catan",
                                test_description, List.of("INVALID_GENRE"), List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void create_listing_blank_title_throws() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", test_price, "",
                                test_description, test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void create_RENTAL_listing_missing_period_throws() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "rental", test_price, "Catan",
                                test_description, test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void create_listing_invalid_item_type_throws() {
                ListingRequest req = new ListingRequest(
                                "INVALID_TYPE", "sale", test_price, "Catan",
                                test_description, test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void create_listing_invalid_listing_type_throws() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "INVALID_TYPE", test_price, "Catan",
                                test_description, test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void create_RENTAL_listing_start_after_end_throws() {
                List<String> rentalPeriod = List.of("2026-06-07", "2026-06-01");
                ListingRequest req = new ListingRequest(
                                "boardgame", "rental", test_price, "Catan",
                                test_description, test_genres, rentalPeriod);

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);

                assertThrows(RuntimeException.class,
                                () -> listingService.createListing(req, test_token, test_image));
                verify(listingRepository, never()).save(any());
        }

        // DELETE

        @Test
        void delete_listing_owner_succeeds() {
                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.of(savedListing));

                listingService.deleteListing(test_listing_id, test_token);

                verify(listingRepository, times(1)).deleteById(test_listing_id);
        }

        @Test
        void delete_listing_not_owner_throws_forbidden() {
                when(jwtService.extractUsername(test_token)).thenReturn("someOtherUser");
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.of(savedListing));

                assertThrows(ForbiddenException.class,
                                () -> listingService.deleteListing(test_listing_id, test_token));
                verify(listingRepository, never()).deleteById(any());
        }

        @Test
        void delete_listing_not_found_throws() {
                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.empty());

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.deleteListing(test_listing_id, test_token));
                verify(listingRepository, never()).deleteById(any());
        }

        // GET BY ID

        @Test
        void getListingById_returns_listing_when_found() {
                when(listingRepository.existsById(test_listing_id)).thenReturn(true);
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.of(savedListing));

                ListingResponse response = listingService.getListingById(test_listing_id);

                assertNotNull(response);
                assertEquals("Catan", response.gameTitle());
        }

        @Test
        void getListingById_throws_when_not_found() {
                when(listingRepository.existsById(test_listing_id)).thenReturn(false);

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.getListingById(test_listing_id));
        }

        // GET ALL ACTIVE
        //

        @Test
        void getAllActiveListings_returns_only_available() {
                when(listingRepository.findByStatus(ListingStatus.AVAILABLE))
                                .thenReturn(List.of(savedListing));

                List<ListingResponse> results = listingService.getAllActiveListings();

                assertEquals(1, results.size());
                assertEquals(ListingStatus.AVAILABLE, results.get(0).status());
        }

        // GET USER LISTINGS
        //

        @Test
        void getUserListings_returns_listings_for_user() {
                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.findByUsername(test_username)).thenReturn(List.of(savedListing));

                List<ListingResponse> results = listingService.getUserListings(test_token);

                assertEquals(1, results.size());
                assertEquals(test_username, results.get(0).username());
        }

        @Test
        void getUserListings_returns_empty_when_no_listings() {
                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.findByUsername(test_username)).thenReturn(List.of());

                List<ListingResponse> results = listingService.getUserListings(test_token);

                assertTrue(results.isEmpty());
        }

        // UPDATE

        @Test
        void updateListing_owner_succeeds() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", 300.00, "Catan Updated",
                                "New description", test_genres, List.of());

                Listing updatedListing = new Listing(
                                test_listing_id, test_username, "boardgame", "sale",
                                300.00, "Catan Updated", "New description", null,
                                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                                test_genres, null);

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.of(savedListing));
                when(listingRepository.existsById(test_listing_id)).thenReturn(true);
                when(listingRepository.save(any())).thenReturn(updatedListing);

                ListingResponse response = listingService.updateListing(test_listing_id, req, test_token);

                assertNotNull(response);
                assertEquals("Catan Updated", response.gameTitle());
                assertEquals(300.00, response.price());
        }

        @Test
        void updateListing_not_owner_throws_forbidden() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", 300.00, "Catan Updated",
                                "New description", test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn("someOtherUser");
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.of(savedListing));

                assertThrows(ForbiddenException.class,
                                () -> listingService.updateListing(test_listing_id, req, test_token));
                verify(listingRepository, never()).save(any());
        }

        @Test
        void updateListing_not_found_throws() {
                ListingRequest req = new ListingRequest(
                                "boardgame", "sale", 300.00, "Catan Updated",
                                "New description", test_genres, List.of());

                when(jwtService.extractUsername(test_token)).thenReturn(test_username);
                when(listingRepository.findById(test_listing_id)).thenReturn(Optional.empty());

                assertThrows(IllegalArgumentException.class,
                                () -> listingService.updateListing(test_listing_id, req, test_token));
                verify(listingRepository, never()).save(any());
        }
}