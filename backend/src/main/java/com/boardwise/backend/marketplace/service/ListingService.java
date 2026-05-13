package com.boardwise.backend.marketplace.service;

import com.boardwise.backend.marketplace.enums.ListingStatus;
import com.boardwise.backend.marketplace.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.ItemType;
import com.boardwise.backend.marketplace.enums.ListingType;
import com.boardwise.backend.marketplace.repository.ListingRepository;

@Service
public class ListingService {

    private final ListingRepository listingRepository;

    public ListingService(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    public ListingResponse createListing(ListingRequest req) {

        String userId = req.userId();
        String gameId = req.gameId();
        ItemType itemType = req.itemType();
        ListingType listingType = req.listingType();
        double price = req.price();
        String description = req.description();// max word limit
        String imageUrl = req.imageUrl();
        List<String> genres = req.genres();
        String gameTitle = req.gameTitle();

        String[] rentalPeriod = req.rentalPeriod();

        if (listingType == ListingType.RENTAL && rentalPeriod == null) {
            throw new IllegalArgumentException("Rental period required for rental listings");
        }

        RentalPeriod borrowDate = null;

        try {

            if (ListingType.RENTAL == listingType) {
                if (rentalPeriod.length != 2) {
                    throw new RuntimeException("only 2 dates must be passed in.");
                }
                borrowDate = new RentalPeriod();

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate start = LocalDate.parse(rentalPeriod[0], dateFormatter);
                LocalDate end = LocalDate.parse(rentalPeriod[1], dateFormatter);

                int comp = start.compareTo(end);

                if (comp > 0)
                    throw new RuntimeException("Start date cannot after End date");

                LocalDate today = LocalDate.now();

                if (today.compareTo(start) > 0)
                    throw new RuntimeException("Start Date cannot a past date");

                if (today.compareTo(end) > 0)
                    throw new RuntimeException("End Date cannot a past date");

                borrowDate.setStartDate(start);
                borrowDate.setEndDate(end);

            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;// should be an error
        }

        // DateTimeFormatter formatter =
        // DateTimeFormatter.ofPattern("dd-MM-yyyyTHH:mm:ss");
        // String date = LocalDateTime.now().format(formatter);

        LocalDateTime now = LocalDateTime.now();
        ListingStatus status = ListingStatus.AVAILABLE;

        Listing toSave = new Listing(null, userId, gameId, itemType, listingType, price, gameTitle, description,
                imageUrl,
                status, now, now, genres, borrowDate);

        Listing saved = listingRepository.save(toSave);

        return mapToResponse(saved);

    }

    private ListingResponse mapToResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getUserId(),
                listing.getTitle(),
                listing.getItemType(),
                listing.getListingType(),
                listing.getPrice(),
                listing.getDescription(),
                listing.getImageUrl(),
                listing.getGenres(),
                listing.getRentalPeriod(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),
                listing.getStatus());
    }

}
