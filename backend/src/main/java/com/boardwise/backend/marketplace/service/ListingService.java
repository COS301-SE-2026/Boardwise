package com.boardwise.backend.marketplace.service;

import com.boardwise.backend.marketplace.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.*;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.shared.security.JWTService;

import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
public class ListingService {

    @Value("${r2.bucket-listings}")
    private String listingsBucket;

    private final ListingRepository listingRepository;
    private final JWTService jwtService;
    private final S3Client s3Client;

    public ListingService(ListingRepository listingRepository, JWTService jwtService, S3Client s3Client) {
        this.listingRepository = listingRepository;
        this.jwtService = jwtService;
        this.s3Client = s3Client;
    }

    private static String truncateAfterWords(String text, int wordLimit) {
        if (text == null || wordLimit <= 0)
            return "";

        // remove n > 2 spaces n = 1
        String doubleSpacingCheck = "\\s{2,}";
        text = text.trim().replaceAll(doubleSpacingCheck, " ");

        String[] words = text.split("\\s+");

        if (words.length <= wordLimit) {
            return text;
        }

        String[] kept = Arrays.copyOfRange(words, 0, wordLimit);
        return String.join(" ", kept);
    }

    public String uploadImageToR2(
            String bucket,
            String listingId,
            MultipartFile file) throws IOException {

        String key = "listings/" + listingId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        return "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/" + key;
    }

    public ListingResponse createListing(ListingRequest req, String token, @RequestPart MultipartFile img) {
        String username = jwtService.extractUsername(token); // fails at filter level

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

        String imageUrl;
        List<String> genres = req.genres();

        // Sanity check
        for (int i = 0; i < genres.size(); i++)
            Genres.fromValue(genres.get(i)).getValue();

        String gameTitle = req.gameTitle();

        // TODO: check if title is avaliable if not upload to db

        if (gameTitle == null || gameTitle.isBlank()) {
            throw new IllegalArgumentException("Game Title cannot be blank");

        }

        List<String> rentalPeriod = (req.rentalPeriod() == null || req.rentalPeriod().size() != 2) ? null
                : req.rentalPeriod();

        RentalPeriod borrowDate = null;

        if (ListingType.RENTAL.equals(ListingType.fromValue(listingType))) {

            if (rentalPeriod == null) {
                throw new IllegalArgumentException("Rental period required for rental listings");
            }
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

        Listing toSave = new Listing(null, username, itemType, listingType, price, gameTitle,
                description,
                null,
                status, now, now, genres, borrowDate);

        Listing saved = listingRepository.save(toSave);

        // TODO: Uncomment

        try {
            imageUrl = uploadImageToR2(listingsBucket, saved.getId(), img);
            saved.setImageUrl(imageUrl);
            listingRepository.save(saved);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapToResponse(saved);

    }

    public List<ListingResponse> getAllActiveListings() {
        return listingRepository.findByStatus(ListingStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteListing(String listingId, String token) {
        String username = jwtService.extractUsername(token);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));

        if (!listing.getUsername().equals(username)) {
            throw new ForbiddenException("You do not own listing: " + listingId);
        }

        listingRepository.deleteById(listingId);

    }

    public ListingResponse getListingById(String id) {
        if (!listingRepository.existsById(id)) {
            throw new IllegalArgumentException("Listing not found: " + id);
        }
        return mapToResponse(listingRepository.findById(id).get());
    }

    public List<ListingResponse> getByFilter(String listingType, String itemType, Double minPrice, Double maxPrice,
            List<String> genres) {
        return listingRepository.findAll().stream()
                .filter(listing -> listing.getStatus() == ListingStatus.AVAILABLE)
                .filter(listing -> listingType == null || listing.getListingType().equalsIgnoreCase(listingType))
                .filter(listing -> itemType == null || listing.getItemType().equalsIgnoreCase(itemType))
                .filter(listing -> minPrice == null || listing.getPrice() >= minPrice)
                .filter(listing -> maxPrice == null || listing.getPrice() <= maxPrice).filter(listing -> genres == null
                        || genres.isEmpty() || listing.getGenres().stream().anyMatch(genres::contains))
                .map(this::mapToResponse).toList();
    }

    public ListingResponse updateListing(String listingId, ListingRequest req, String token) {
        String username = jwtService.extractUsername(token);

        Listing existing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));

        if (!existing.getUsername().equals(username)) {
            throw new ForbiddenException("You do not own listing: " + listingId);
        }
        if (!listingRepository.existsById(listingId)) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }

        existing.setItemType(req.itemType());
        existing.setListingType(req.listingType());
        existing.setPrice(req.price());
        existing.setTitle(req.gameTitle());
        existing.setDescription(truncateAfterWords(req.description(), 500));
        // existing.setImageUrl(req.imageUrl());
        // TODO: add replace image
        existing.setGenres(req.genres());
        existing.setUpdatedAt(LocalDateTime.now());

        if (req.rentalPeriod() != null && !req.rentalPeriod().isEmpty()) {

            if (req.rentalPeriod().size() != 2) {
                throw new IllegalArgumentException("Only 2 dates are allowed");
            }

            List<String> rentalPeriod = req.rentalPeriod();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(rentalPeriod.get(0), dateFormatter);
            LocalDate end = LocalDate.parse(rentalPeriod.get(1), dateFormatter);

            if (start.compareTo(end) > 0)
                throw new IllegalArgumentException("Start date cannot be after End date");

            RentalPeriod borrowDate = new RentalPeriod();
            borrowDate.setStartDate(start);
            borrowDate.setEndDate(end);
            existing.setRentalPeriod(borrowDate);
        }

        Listing saved = listingRepository.save(existing);
        return mapToResponse(saved);
    }

    public List<ListingResponse> getUserListings(String token) {
        String username = jwtService.extractUsername(token);
        // if (!userRepository.existsById(id)) {
        // throw new IllegalArgumentException("User does not exist");
        // }

        return listingRepository.findByUsername(username).stream().map(this::mapToResponse).toList();
    }

    private ListingResponse mapToResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getUsername(),
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
