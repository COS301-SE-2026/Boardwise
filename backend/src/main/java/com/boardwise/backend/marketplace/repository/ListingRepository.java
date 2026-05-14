package com.boardwise.backend.marketplace.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.marketplace.model.Listing;
import java.util.List;
import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;

@Repository
public interface ListingRepository extends MongoRepository<Listing, String> {
    List<Listing> findByUserId(String userId);

    List<Listing> findByGameId(String gameId);

    List<Listing> findByItemType(ItemType itemType);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByListingType(ListingType listingType);

}
