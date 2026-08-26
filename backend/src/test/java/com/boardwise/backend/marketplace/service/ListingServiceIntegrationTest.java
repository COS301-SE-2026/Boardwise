package com.boardwise.backend.marketplace.service;

import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.*;

import com.boardwise.backend.BaseIntegrationTest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.Condition;
import com.boardwise.backend.marketplace.enums.Genres;
import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.models.Listing;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.marketplace.service.ListingService;

public class ListingServiceIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setUp() {
        listingRepository.deleteAll();
    }

    @Autowired
    private ListingService listingService;

    @Autowired
    private ListingRepository listingRepository;

    @Test
    void shouldStoreAndRetrieveListing(){
        Listing listing = new Listing("rtickyv", "IamR3al", new ObjectId(), ItemType.FullBoardGame.getValue(), "sale", 50.0, "Hatfied", true, "title", Condition.FAIR.name(), "Monopoly", "gg", "desc", "image", ListingStatus.AVAILABLE, LocalDateTime.now(), LocalDateTime.now(), List.of(Genres.ABSTRACT.getValue()), null);
        listingRepository.save(listing);
        List<ListingResponse> results = listingService.getAllActiveListings();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).gameTitle()).isEqualTo("Monopoly");
        assertThat(results.get(0).itemType()).isEqualTo(ItemType.FullBoardGame.getValue());
    }

    

    
}
