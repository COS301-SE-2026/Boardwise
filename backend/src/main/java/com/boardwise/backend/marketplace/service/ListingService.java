package com.boardwise.backend.marketplace.service;

import com.boardwise.backend.marketplace.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.*;

@Service
public class ListingService {

    @Value("${r2.bucket-listings}")
    private String listingsBucket;

    @Value("${r2.dev-url}")
    private String publicUrl;

    private final ListingRepository listingRepository;
    private final JWTService jwtService;
    private final S3Client s3Client;
    private final MongoTemplate mongoTemplate;

    private final String defaultImage = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/default-listing-images/default.png";// in frontend Images

    public ListingService(ListingRepository listingRepository, JWTService jwtService, S3Client s3Client, MongoTemplate mongoTemplate) {
        this.listingRepository = listingRepository;
        this.jwtService = jwtService;
        this.s3Client = s3Client;
        this.mongoTemplate = mongoTemplate;
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
            String listingId,
            MultipartFile file) throws IOException {

        String key = "listings/" + listingId + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(listingsBucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build(),
                RequestBody.fromBytes(file.getBytes()));

        return publicUrl + key;
    }

    public void deleteFile(String fileName) {
        // no file provided to delete: theoretically shouldn't ever be triggered
        if (fileName == null || fileName.isBlank()) {
            return;
        }

        fileName = fileName.substring(publicUrl.length());
        // request object
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(listingsBucket)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    public String replaceFile(String currentFile, String listingId, MultipartFile targetFile) throws IOException {
        // currentFile is the current value already stored on the buckets
        // targetFile is the file replacing current

        // remove existing entry
        deleteFile(currentFile);
        return uploadImageToR2(listingId, targetFile);
    }

    public ListingResponse createListing(ListingRequest req, String token, @RequestPart MultipartFile img) {
        
        ObjectId userId = jwtService.extractUserId(token); // fails at filter level

        String itemType = req.itemType().trim();

        // Sanity check
        ItemType.fromValue(itemType);

        String listingType = req.listingType().trim();

        // Sanity check
        ListingType.fromValue(listingType);

        // Sanity check
        String condition = req.condition();
        Condition.fromValue(condition);

        double price = req.price();
        if (price < 0) {
            throw new IllegalArgumentException("Negative pricing is not allowed");
        }

        String description = truncateAfterWords(req.description().trim(), 500);

        String imageUrl;

        String listingTitle = req.listingTitle().trim();

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
        // TODO: uncomment
        // String location = jwtService.extractLocation(token);
        // TODO: delete when extract location is acquired
        String location = req.location();

        // TODO:Verify by checking if version is available in db else add it
        String version = req.version();

        Listing toSave = new Listing(null, jwtService.extractUsername(token).toString(),userId, itemType,
                listingType, price, location, req.isNegotiable(),listingTitle, condition, gameTitle, version,
                description,
                null,
                status, now, now, genres, borrowDate);

        Listing saved = listingRepository.save(toSave);

        if (img != null && !img.isEmpty()) {
            try {
                imageUrl = uploadImageToR2(saved.getId(), img);
                saved.setImageUrl(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //failsafe
        else{
            imageUrl = defaultImage;
            saved.setImageUrl(imageUrl);
        }

        listingRepository.save(saved);

        return mapToResponse(saved);

    }

    public List<ListingResponse> getAllActiveListings() {
        return listingRepository.findByStatus(ListingStatus.AVAILABLE)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public void deleteListing(String listingId, String token) {
        ObjectId userId = jwtService.extractUserId(token);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));

        if (!listing.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not own listing: " + listingId);
        }

        //trying not to delete the stored image 
        if(!listing.getImageUrl().equals(defaultImage))deleteFile(listing.getImageUrl());
        listingRepository.deleteById(listingId);

    }

    public ListingResponse getListingById(String listingId) {
        if (!listingRepository.existsById(listingId)) {
            throw new IllegalArgumentException("Listing not found: " + listingId);
        }
        return mapToResponse(listingRepository.findById(listingId).get());
    }

    public Page<ListingResponse> getByFilter(String gameTitle, String listingTitle ,String listingType, String itemType, Double minPrice, Double maxPrice, List<String> conditions, List<String> genres, Integer page, Integer size) {
        
        //Search for AVAILABLE Listings 
        Criteria criteria = Criteria.where("status").is(ListingStatus.AVAILABLE);

        // if(listingTitle != null) criteria.and("listingTitle").regex(listingTitle, "i");

        if(gameTitle != null) criteria.and("gameTitle").regex(gameTitle, "i");

        if (listingType != null)criteria.and("listingType").regex(listingType, "i");

        if (itemType != null) criteria.and("itemType").regex(itemType, "i");

        if (minPrice != null && maxPrice != null) criteria.and("price").gte(minPrice).lte(maxPrice);
        //minimum and up
        else if (minPrice != null) criteria.and("price").gte(minPrice);
        //maximum and down
        else if (maxPrice != null) criteria.and("price").lte(maxPrice);

        if (genres != null && !genres.isEmpty())criteria.and("genres").in(genres);

        if(conditions != null && !conditions.isEmpty()) criteria.and("condition").in(conditions);

        PageRequest pageRequest = null;
        Query query = new Query(criteria);
        if(page != null && size != null){
            if(page < 0 ) page = 0;
            if(size < 0) size = Integer.MAX_VALUE;

            //Pagination
            pageRequest = PageRequest.of((page == 0)? 0 : page - 1 ,size); 
            query.with(pageRequest);

        }

        if(pageRequest == null){
            pageRequest = PageRequest.of(0, Integer.MAX_VALUE);
        }

        long total = mongoTemplate.count(new Query(criteria), Listing.class);

        List<ListingResponse> res = mongoTemplate.find(query,Listing.class).stream().map(this::mapToResponse).toList();

        return  new PageImpl<>(res, pageRequest, total);
    }

    public ListingResponse updateListing(String listingId, ListingRequest req, String token, MultipartFile img) {
        ObjectId userId = jwtService.extractUserId(token);

        Listing existing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Listing not found: " + listingId));

        if (!userId.equals(existing.getUserId())) {
            throw new ForbiddenException("Cannot update " + listingId);
        }

        // sanity check
        if (req != null && !req.itemType().equals(existing.getItemType())) {
            ItemType.fromValue(req.itemType());
            existing.setItemType(req.itemType());
        }

        // sanity check
        if (!req.condition().equals(existing.getCondition())) {
            existing.setCondition(req.condition());
        }
        ;

        // sanity check
        if (!req.listingType().equals(existing.getListingType())) {
            ListingType.fromValue(req.listingType());
            existing.setListingType(req.listingType());
        }

        double priceToAdd = req.price();
        if (priceToAdd != existing.getPrice()) {
            if (priceToAdd <= 0) { // bind it to curr
                priceToAdd = existing.getPrice();
            }
            existing.setPrice(req.price());
        }

        // TODO: Uncomment when frontend automatically stores location
        // String userLocation = jwtService.extractLocation(token);
        // if(!existing.getLocation().equals((userLocation.isBlank())?null:
        // userLocation)){
        // existing.setLocation(token);
        // }

        if (!existing.getLocation().equals(req.location())) {
            existing.setLocation(req.location());
        }
        // TODO: verify Title (check if title is in game list)
        if (!existing.getGameTitle().equals(req.gameTitle())) {
            existing.setGameTitle(req.gameTitle());
        }

        if (!req.description().isBlank() && !req.description().equals(existing.getDescription()))
            existing.setDescription(truncateAfterWords(req.description(), 500));

        // sanity check
        if (!req.genres().equals(existing.getGenres())) {
            // just validating genres
            for (int i = 0; i < req.genres().size(); i++)
                Genres.fromValue(req.genres().get(i)).getValue();

            existing.setGenres(req.genres());
        }

        if (!req.condition().equals(existing.getCondition())) {
            Condition.fromValue(req.condition()).getValue();
            existing.setCondition(req.condition());
        }

        if (img != null && !img.isEmpty()) {// only update if img is there
            String imageUrl;
            try {
                if(!existing.getImageUrl().equals(defaultImage)){
                    imageUrl= replaceFile(existing.getImageUrl(), existing.getId(), img);
                }
                else{
                    imageUrl = uploadImageToR2(listingId, img);
                }
                existing.setImageUrl(imageUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }

        String imgAsString = img.getOriginalFilename().toLowerCase(); 

        if(!imgAsString.contains(".png") && !imgAsString.contains(".jpg") && !imgAsString.contains(".jpeg") && !imgAsString.contains(".webp")){
            throw new IllegalArgumentException("Not a valid image file");
        }

        if (existing.getListingType().equalsIgnoreCase(ListingType.RENTAL.getValue())) {
            if (req.rentalPeriod() != null && !req.rentalPeriod().isEmpty()) {
                if (req.rentalPeriod().size() != 2) {
                    throw new IllegalArgumentException("Only 2 dates are allowed");
                }

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate start = LocalDate.parse(req.rentalPeriod().get(0), dateFormatter);
                LocalDate end = LocalDate.parse(req.rentalPeriod().get(1), dateFormatter);

                if (start.compareTo(end) > 0)
                    throw new IllegalArgumentException("Start date cannot be after End date");

                RentalPeriod borrowDate = new RentalPeriod();
                borrowDate.setStartDate(start);
                borrowDate.setEndDate(end);
                existing.setRentalPeriod(borrowDate);
            }
        }

        if (!existing.getVersion().equals(req.version())) {
            // TODO:implement check to see if version is alr on db or add it to db

            existing.setVersion(req.version());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        existing.setIsNegotiable(req.isNegotiable());

        return mapToResponse(listingRepository.save(existing));
    }

    public List<ListingResponse> getUserListings(String token) {
        String username = jwtService.extractUsername(token);
        return listingRepository.findByUsername(username).stream().map(this::mapToResponse).toList();
    }

    private ListingResponse mapToResponse(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getListingTitle(),
                listing.getUsername(),
                listing.getUserId(),
                listing.getGameTitle(),
                listing.getItemType(),
                listing.getListingType(),
                listing.getPrice(),
                listing.getDescription(),
                listing.getImageUrl(),
                listing.getLocation(),
                listing.getIsNegotiable(),
                listing.getCondition(),
                listing.getVersion(),
                listing.getGenres(),
                listing.getRentalPeriod(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),
                listing.getStatus());
    }

}
