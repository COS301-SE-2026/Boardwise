package com.boardwise.backend.marketplace.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.boardwise.backend.marketplace.model.Listing;
import java.util.List;
import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.enums.ListingType;
import org.bson.types.ObjectId;

@Repository
public interface ListingRepository extends MongoRepository<Listing, String> {

    List<Listing> findByItemType(String itemType);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByListingType(ListingType listingType);

    List<Listing> findByUsername(String username);

    List<Listing> findByUserId(ObjectId userId);
}
