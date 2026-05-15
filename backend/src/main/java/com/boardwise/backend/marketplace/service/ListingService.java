package com.boardwise.backend.marketplace.service;

import com.boardwise.backend.marketplace.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.*;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingService(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    public static String truncateAfterWords(String text, int wordLimit) {
        if (text == null || wordLimit <= 0)
            return "";

        String[] words = text.split("\\s+");

        if (words.length <= wordLimit) {
            return text;
        }

        String[] kept = Arrays.copyOfRange(words, 0, wordLimit);
        return String.join(" ", kept);
    }

    public ListingResponse createListing(ListingRequest req) {

        String userId = req.userId();

        // if (!userRepository.existsById(userId)) {
        // throw new IllegalArgumentException("User does not exist");
        // }

        String gameId = req.gameId();

        String itemType = req.itemType();

        // Sanity check
        ItemType.fromValue(itemType);

        String listingType = req.listingType();

        // Sanity check
        ListingType.fromValue(listingType);

        double price = req.price();
        if (price < 0) {
            throw new IllegalArgumentException("Negative pricing is not allowed");
        }

        String description = truncateAfterWords(req.description(), 500);

        String imageUrl = req.imageUrl();

        List<String> genres = req.genres();

        // Sanity check
        for (int i = 0; i < genres.size(); i++)
            Genres.fromValue(genres.get(i)).getValue();

        String gameTitle = req.gameTitle();

        List<String> rentalPeriod = req.rentalPeriod();

        if (ListingType.fromValue(listingType).equals(ListingType.RENTAL) && rentalPeriod == null) {
            throw new IllegalArgumentException("Rental period required for rental listings");
        }

        RentalPeriod borrowDate = null;

        if (ListingType.RENTAL.equals(ListingType.fromValue(listingType))) {

            if (rentalPeriod.size() != 2) {
                throw new RuntimeException("only 2 dates must be passed in.");
            }
            borrowDate = new RentalPeriod();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate start = LocalDate.parse(rentalPeriod.toArray()[0].toString(), dateFormatter);
            LocalDate end = LocalDate.parse(rentalPeriod.toArray()[1].toString(), dateFormatter);

            int comp = start.compareTo(end);

            if (comp > 0)
                throw new RuntimeException("Start date cannot be after End date");

            LocalDate today = LocalDate.now();

            if (today.compareTo(start) > 0)
                throw new RuntimeException("Start Date cannot be a past date");

            if (today.compareTo(end) > 0)
                throw new RuntimeException("End Date cannot be a past date");

            borrowDate.setStartDate(start);
            borrowDate.setEndDate(end);

        }

        LocalDateTime now = LocalDateTime.now();
        ListingStatus status = ListingStatus.AVAILABLE;

        Listing toSave = new Listing(null, userId, gameId, itemType, listingType, price, gameTitle,
                description,
                imageUrl,
                status, now, now, genres, borrowDate);

        Listing saved = listingRepository.save(toSave);

        return mapToResponse(saved);

    }

    public List<ListingResponse> getAllActiveListings() {
        return listingRepository.findByStatus(ListingStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteListing(String id) {
        if (id == (null) || !listingRepository.existsById(id)) {
            throw new IllegalArgumentException("Listing not found: " + id);
        }
        listingRepository.deleteById(id);
    }

    public ListingResponse getListingById(String id) {
        if (!listingRepository.existsById(id)) {
            throw new IllegalArgumentException("Listing not found: " + id);
        }
        return mapToResponse(listingRepository.findById(id).get());
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
