package com.boardwise.backend.marketplace;

import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.Listing;
import com.boardwise.backend.marketplace.repository.ListingRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test") // <--- ADD THIS LINE
public class ListingRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");


    @Autowired
    private ListingRepository listingRepository;

    private Listing testListing;

    @BeforeEach
    void setUp() {
        testListing = new Listing(
                null, "testuser", "boardgame", "sale",
                250.00, "Catan", "Barely used copy of Catan", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("economic"), null);
    }

    @AfterEach
    void tearDown() {
        listingRepository.deleteAll();
    }

    // --- SAVE ---------------------------------------------------------------------

    @Test
    void save_listing_persists_to_db() {
        Listing saved = listingRepository.save(testListing);

        assertNotNull(saved.getId());
        assertEquals("Catan", saved.getTitle());
        assertEquals("testuser", saved.getUsername());
        assertEquals(ListingStatus.AVAILABLE, saved.getStatus());
    }

    // --- FIND BY ID ---------------------------------------------------------------

    @Test
    void findById_returns_listing_when_exists() {
        Listing saved = listingRepository.save(testListing);

        Optional<Listing> found = listingRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Catan", found.get().getTitle());
    }

    @Test
    void findById_returns_empty_when_not_exists() {
        Optional<Listing> found = listingRepository.findById("nonexistent-id");

        assertTrue(found.isEmpty());
    }

    // --- FIND BY STATUS -----------------------------------------------------------

    @Test
    void findByStatus_returns_only_available_listings() {
        listingRepository.save(testListing);

        Listing soldListing = new Listing(
                null, "testuser", "boardgame", "sale",
                100.00, "Monopoly", "Old Monopoly", null,
                ListingStatus.SOLD, LocalDateTime.now(), LocalDateTime.now(),
                List.of("economic"), null);
        listingRepository.save(soldListing);

        List<Listing> available = listingRepository.findByStatus(ListingStatus.AVAILABLE);

        assertEquals(1, available.size());
        assertEquals("Catan", available.get(0).getTitle());
    }

    @Test
    void findByStatus_returns_empty_when_none_match() {
        // Only a SOLD listing exists; querying AVAILABLE should return nothing
        Listing soldListing = new Listing(
                null, "testuser", "boardgame", "sale",
                100.00, "Monopoly", "Old Monopoly", null,
                ListingStatus.SOLD, LocalDateTime.now(), LocalDateTime.now(),
                List.of("economic"), null);
        listingRepository.save(soldListing);

        List<Listing> available = listingRepository.findByStatus(ListingStatus.AVAILABLE);

        assertTrue(available.isEmpty());
    }

    // --- FIND BY USERNAME ---------------------------------------------------------

    @Test
    void findByUsername_returns_listings_for_user() {
        listingRepository.save(testListing);

        Listing otherUserListing = new Listing(
                null, "otheruser", "boardgame", "sale",
                100.00, "Monopoly", "Old Monopoly", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("economic"), null);
        listingRepository.save(otherUserListing);

        List<Listing> userListings = listingRepository.findByUsername("testuser");

        assertEquals(1, userListings.size());
        assertEquals("testuser", userListings.get(0).getUsername());
    }

    @Test
    void findByUsername_returns_empty_when_no_listings() {
        List<Listing> userListings = listingRepository.findByUsername("nobody");

        assertTrue(userListings.isEmpty());
    }

    @Test
    void findByUsername_returns_multiple_listings_for_same_user() {
        listingRepository.save(testListing);

        Listing secondListing = new Listing(
                null, "testuser", "boardgame", "rental",
                50.00, "Ticket to Ride", "Great condition", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("negotiation"), null);
        listingRepository.save(secondListing);

        List<Listing> userListings = listingRepository.findByUsername("testuser");

        assertEquals(2, userListings.size());
        assertTrue(userListings.stream().allMatch(l -> l.getUsername().equals("testuser")));
    }

    // --- DELETE -------------------------------------------------------------------

    @Test
    void deleteById_removes_listing_from_db() {
        Listing saved = listingRepository.save(testListing);
        String id = saved.getId();

        listingRepository.deleteById(id);

        assertFalse(listingRepository.existsById(id));
    }

    // --- EXISTS -------------------------------------------------------------------

    @Test
    void existsById_returns_true_when_listing_exists() {
        Listing saved = listingRepository.save(testListing);

        assertTrue(listingRepository.existsById(saved.getId()));
    }

    @Test
    void existsById_returns_false_when_listing_not_exists() {
        assertFalse(listingRepository.existsById("nonexistent-id"));
    }

    // --- FIND ALL -----------------------------------------------------------------

    @Test
    void findAll_returns_all_listings() {
        listingRepository.save(testListing);

        Listing secondListing = new Listing(
                null, "testuser", "boardgame", "rental",
                50.00, "Ticket to Ride", "Great condition", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("negotiation"), null);
        listingRepository.save(secondListing);

        List<Listing> all = listingRepository.findAll();

        assertEquals(2, all.size());
    }

    // --- GENRES -------------------------------------------------------------------

    @Test
    void save_listing_with_multiple_genres_persists_correctly() {
        testListing = new Listing(
                null, "testuser", "boardgame", "sale",
                250.00, "Catan", "Barely used copy of Catan", null,
                ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(),
                List.of("economic", "negotiation", "dice"), null);

        Listing saved = listingRepository.save(testListing);

        assertEquals(3, saved.getGenres().size());
        assertTrue(saved.getGenres().contains("economic"));
        assertTrue(saved.getGenres().contains("negotiation"));
        assertTrue(saved.getGenres().contains("dice"));
    }

    // --- UPDATE -------------------------------------------------------------------

    @Test
    void save_updated_listing_reflects_changes() {
        Listing saved = listingRepository.save(testListing);

        saved.setPrice(999.00);
        saved.setStatus(ListingStatus.SOLD);
        listingRepository.save(saved);

        Listing reloaded = listingRepository.findById(saved.getId()).orElseThrow();
        assertEquals(999.00, reloaded.getPrice());
        assertEquals(ListingStatus.SOLD, reloaded.getStatus());
    }
}