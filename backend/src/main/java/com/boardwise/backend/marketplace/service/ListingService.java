package com.boardwise.backend.marketplace.service;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;

import com.boardwise.backend.marketplace.dtos.listing.ListingRequest;
import com.boardwise.backend.marketplace.dtos.listing.ListingResponse;
import com.boardwise.backend.marketplace.enums.*;
import com.boardwise.backend.marketplace.exceptions.ForbiddenException;
import com.boardwise.backend.marketplace.exceptions.ResourceNotFound;
import com.boardwise.backend.marketplace.models.*;
import com.boardwise.backend.marketplace.repository.ListingRepository;
import com.boardwise.backend.shared.repository.BoardGameRepository;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.model.Boardgame;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.UserRepository;

import org.bson.types.ObjectId;
import org.owasp.encoder.Encode;
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

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".webp");

    @Value("${r2.bucket-listings}")
    private String listingsBucket;

    @Value("${r2.listings.public-url}")
    private String publicUrl;

    private final ListingRepository listingRepository;
    private final JWTService jwtService;
    private final S3Client s3Client;
    private final MongoTemplate mongoTemplate;
    private final UserRepository userRepository;
    private final BoardGameRepository boardGameRepository;


    private final String defaultImage = "https://pub-c543dd80255b4b9c9c31a54e09389b5d.r2.dev/default-listing-images/default.png";//on bucket NEVER DELETE

    public ListingService(ListingRepository listingRepository, JWTService jwtService, S3Client s3Client, MongoTemplate mongoTemplate, UserRepository userRepository,BoardGameRepository boardGameRepository) {
        this.listingRepository = listingRepository;
        this.jwtService = jwtService;
        this.s3Client = s3Client;
        this.mongoTemplate = mongoTemplate;
        this.userRepository = userRepository;
        this.boardGameRepository = boardGameRepository;
    }

    public static String sanitize(String input) {
        if (input == null) return null;

        // trim whitespace
        String sanitized = input.trim();

        // strip HTML tags
        sanitized = sanitized.replaceAll("<[^>]*>", "");

        // encode any remaining special characters
        sanitized = Encode.forHtml(sanitized);

        // block NoSQL injection operators
        if (sanitized.contains("$") || sanitized.contains("{")) {
            throw new IllegalArgumentException("Invalid characters in input");
        }

        return sanitized;
    }
    
    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return sanitize(value.trim());
    }

    private static String validateImageExtension(String originalFilename) {
        String name = sanitize(originalFilename);
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Invalid image file");
        }
        name = name.toLowerCase(); // accounting for capitalised extensions
        boolean hasAllowedExtension = ALLOWED_IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
        if (!hasAllowedExtension) {
            throw new IllegalArgumentException("Invalid image file type. Allowed types: " + ALLOWED_IMAGE_EXTENSIONS);
        }
        return name;
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
        if (fileName == null || fileName.isBlank() || fileName.contains("/seeded-data/")) { // i just hope no one names their file "seeded-data"
            return;
        }

        if (fileName.contains(publicUrl)) {
            fileName = fileName.substring(publicUrl.length());
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(listingsBucket)
        .key(fileName)
        .build();


        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (SdkException e) {
            log.warn("Failed to delete file '{}' from bucket '{}': {}", fileName, listingsBucket, e.getMessage(), e);
        }
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

        String itemType = requireNonBlank(req.itemType(), "Item type");
        ItemType.fromValue(itemType); // sanity check

        String listingType = requireNonBlank(req.listingType(), "Listing type");
        ListingType.fromValue(listingType); // sanity check

        String condition = requireNonBlank(req.condition(), "Condition");
        Condition.fromValue(condition); // sanity check

        double price = req.price();
        if (price < 0) {
            throw new IllegalArgumentException("Negative pricing is not allowed");
        }

        if (req.description() == null || req.description().isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        String description = truncateAfterWords(sanitize(req.description()), 500);

        String listingTitle = requireNonBlank(req.listingTitle(), "Listing title");

        String gameTitle = sanitize(req.gameTitle());
        if (gameTitle == null || gameTitle.isBlank()) {
            throw new IllegalArgumentException("Game Title cannot be blank");
        }

        // check if title is available, if not upload to db
        Optional<Boardgame> validGame = boardGameRepository.findByTitle(gameTitle);
        if (validGame.isEmpty()) {
            Boardgame toBeInserted = new Boardgame(null, null, gameTitle, null, null, 1, 2, 3, null, null);
            boardGameRepository.insert(toBeInserted);
        }

        List<String> rentalPeriod = (req.rentalPeriod() == null || req.rentalPeriod().size() != 2) ? null
                : req.rentalPeriod();

        RentalPeriod borrowDate = null;

        if (ListingType.RENTAL.equals(ListingType.fromValue(listingType))) {

            if (rentalPeriod == null) {
                throw new IllegalArgumentException("Rental period required for rental listings");
            }
            if (rentalPeriod.size() != 2) {
                throw new IllegalArgumentException("only 2 dates must be passed in.");
            }
            borrowDate = new RentalPeriod();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate start;
            LocalDate end;
            try {
                start = LocalDate.parse(rentalPeriod.get(0), dateFormatter);
                end = LocalDate.parse(rentalPeriod.get(1), dateFormatter);
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Rental dates must be in yyyy-MM-dd format");
            }

            int comp = start.compareTo(end);

            if (comp > 0)
                throw new IllegalArgumentException("Start date cannot be after End date");

            LocalDate today = LocalDate.now();

            if (today.compareTo(start) > 0)
                throw new IllegalArgumentException("Start Date cannot be a past date");

            if (today.compareTo(end) > 0)
                throw new IllegalArgumentException("End Date cannot be a past date");

            borrowDate.setStartDate(start);
            borrowDate.setEndDate(end);

        }

        LocalDateTime now = LocalDateTime.now();
        ListingStatus status = ListingStatus.AVAILABLE;
        String location = requireNonBlank(req.location(), "Location");

        String version = requireNonBlank(req.version(), "Version");

        String username = userRepository.findById(userId.toString())
        .orElseThrow(() -> new ResourceNotFound("User not found"))
        .getUsername();

        Listing toSave = new Listing(null,username,userId, itemType,
                listingType, price, location, req.isNegotiable(),listingTitle, condition, gameTitle, version,
                description,
                null,
                status, now, now, borrowDate);

        Listing saved = listingRepository.save(toSave);

        if (img != null && !img.isEmpty()) {
            // throws IllegalArgumentException on a bad extension - fine, listing is
            // already saved with the default image and nothing has leaked yet.
            validateImageExtension(img.getOriginalFilename());

            try {
                String imageUrl = uploadImageToR2(saved.getId(), img);
                saved.setImageUrl(imageUrl);
            } catch (IOException | SdkException e) {
                log.warn("Image upload failed for listing '{}', falling back to default image: {}", saved.getId(), e.getMessage(), e);
                saved.setImageUrl(defaultImage);
            }
        } else {
            saved.setImageUrl(defaultImage);
        }

        listingRepository.save(saved);

        return mapToResponse(saved);

    }

    public List<ListingResponse> getAllActiveListings(String token) {
        List<Listing> listings = listingRepository.findByStatus(ListingStatus.AVAILABLE);
        listings = personalizeOrder(listings, token);
        return listings.stream().map(this::mapToResponse).toList();
    }

    private List<Listing> personalizeOrder(List<Listing> listings, String token) {
        List<String> ownedGames = Collections.emptyList();
        List<String> preferredGenres = Collections.emptyList();

        if (token != null && !token.isBlank()) {
            try {
                ObjectId userId = jwtService.extractUserId(token);
                User user = userRepository.findById(userId.toString()).orElse(null);
                if (user != null) {
                    if (user.getOwnedGames() != null) ownedGames = user.getOwnedGames();
                    if (user.getPreferences() != null && user.getPreferences().getGenres() != null) {
                        preferredGenres = user.getPreferences().getGenres();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to personalize listing order, falling back to default order: {}", e.getMessage(), e);
                return listings;
            }
        }

        if (ownedGames.isEmpty() && preferredGenres.isEmpty()) {
            return listings;
        }

        // lambdas require effectively-final captures
        final List<String> ownedGamesFinal = ownedGames;
        final List<String> preferredGenresFinal = preferredGenres;

        Comparator<Listing> personalizedOrder = Comparator
            .comparing((Listing l) -> ownsGame(l, ownedGamesFinal) ? 0 : 1)
            .thenComparing((Listing l) -> -genreOverlapCount(l, preferredGenresFinal));

        return listings.stream().sorted(personalizedOrder).toList();
    }

    private boolean ownsGame(Listing listing, List<String> ownedGames) {
        if (listing.getGameTitle() == null) return false;
        return ownedGames.stream().anyMatch(owned -> owned.equalsIgnoreCase(listing.getGameTitle()));
    }

    private long genreOverlapCount(Listing listing, List<String> preferredGenres) {
        Optional<Boardgame> validGame = boardGameRepository.findByTitle(listing.getGameTitle());

        if (validGame.isEmpty() || validGame.get().getGenres() == null) return 0;
        return validGame.get().getGenres().stream()
            .filter(g -> preferredGenres.stream().anyMatch(p -> p.equalsIgnoreCase(g)))
            .count();
    }

    public void deleteListing(String listingId, String token) {
        ObjectId userId = jwtService.extractUserId(token);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFound("Listing not found: " + listingId));

        if (!listing.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not own listing: " + listingId);
        }

        // trying not to delete the stored default image
        if (listing.getImageUrl() != null && !listing.getImageUrl().equals(defaultImage)) {
            deleteFile(listing.getImageUrl());
        }
        listingRepository.deleteById(listingId);

    }

    public ListingResponse getListingById(String listingId) {
        return mapToResponse(listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFound("Listing not found: " + listingId)));
    }

    public Page<ListingResponse> getByFilter(String gameTitle, String listingTitle, String listingType,String itemType, Double minPrice, Double maxPrice, List<String> conditions, List<String> genres,
        Integer page, Integer size, String token){
        //Search for AVAILABLE Listings
        Criteria criteria = Criteria.where("status").is(ListingStatus.AVAILABLE);

        // if(listingTitle != null) criteria.and("listingTitle").regex(listingTitle, "i");

        if (gameTitle != null) {
            Criteria searchCriteria = new Criteria().orOperator(
                Criteria.where("gameTitle").regex(gameTitle, "i"),
                Criteria.where("listingTitle").regex(gameTitle, "i")
            );
            criteria = new Criteria().andOperator(criteria, searchCriteria);
        }

        if (listingType != null)criteria.and("listingType").regex(listingType, "i");

        if (itemType != null) criteria.and("itemType").regex(itemType, "i");

        if (minPrice != null && maxPrice != null) criteria.and("price").gte(minPrice).lte(maxPrice);
        //minimum and up
        else if (minPrice != null) criteria.and("price").gte(minPrice);
        //maximum and down
        else if (maxPrice != null) criteria.and("price").lte(maxPrice);

        if (genres != null && !genres.isEmpty())criteria.and("genres").in(genres);

        if (conditions != null && !conditions.isEmpty())criteria.and("condition").in(conditions);


        PageRequest pageRequest;
        Query query = new Query(criteria);
        if(page != null && size != null){
            if(page < 0 ) page = 0;
            if(size < 0) size = Integer.MAX_VALUE;

            //Pagination
            pageRequest = PageRequest.of(page ,size);
            query.with(pageRequest);
        }
        List<Listing> allMatches = mongoTemplate.find(new Query(criteria), Listing.class);
            allMatches = personalizeOrder(allMatches, token);

        int pageNum = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : Math.max(allMatches.size(), 1);

        int fromIndex = Math.min(pageNum * pageSize, allMatches.size());
        int toIndex = Math.min(fromIndex + pageSize, allMatches.size());

        List<ListingResponse> pageContent = allMatches.subList(fromIndex, toIndex).stream().map(this::mapToResponse).toList();

        PageRequest pageReq= PageRequest.of(pageNum, pageSize);

        return new PageImpl<>(pageContent, pageReq, allMatches.size());
    }

    public ListingResponse updateListing(String listingId, ListingRequest req, String token, MultipartFile img) {
        ObjectId userId = jwtService.extractUserId(token);

        Listing existing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFound("Listing not found: " + listingId));

        if (!userId.equals(existing.getUserId())) {
            throw new ForbiddenException("Cannot update " + listingId);
        }

        // sanity check
        if (req.itemType() != null && !req.itemType().equals(existing.getItemType())) {
            ItemType.fromValue(req.itemType());
            existing.setItemType(sanitize(req.itemType()));
        }

        // sanity check
        if (req.condition() != null && !req.condition().equals(existing.getCondition())) {
            Condition.fromValue(req.condition());
            existing.setCondition(sanitize(req.condition()));
        }

        // sanity check
        if (req.listingType() != null && !req.listingType().equals(existing.getListingType())) {
            ListingType.fromValue(req.listingType());
            existing.setListingType(sanitize(req.listingType()));
        }

        double priceToAdd = req.price();
        if (priceToAdd != existing.getPrice()) {
            if (priceToAdd < 0) { // bind it to curr; 0 is a valid ("free") price
                priceToAdd = existing.getPrice();
            }
            existing.setPrice(priceToAdd);
        }

        if (req.location() != null && !existing.getLocation().equals(req.location())) {
            existing.setLocation(sanitize(req.location()));
        }

        if (req.listingTitle() != null && !existing.getListingTitle().equals(req.listingTitle())) {
            existing.setListingTitle(sanitize(req.listingTitle()));
        }

        if (req.gameTitle() != null && !existing.getGameTitle().equals(sanitize(req.gameTitle()))) {
            String newGameTitle = sanitize(req.gameTitle());
            if (newGameTitle.isBlank()) {
                throw new IllegalArgumentException("Game Title cannot be blank");
            }
            Optional<Boardgame> validGame = boardGameRepository.findByTitle(req.gameTitle());
            if (validGame.isEmpty()) {
                Boardgame toBeInserted = new Boardgame(null, null, req.gameTitle(), null, null, 1, 2, 3, null, null);
                boardGameRepository.insert(toBeInserted);
                existing.setGameTitle(newGameTitle);
            } else {
                existing.setGameTitle(sanitize(validGame.get().getTitle()));
            }
        }

        if (req.description() != null && !req.description().isBlank() && !req.description().equals(existing.getDescription()))
            existing.setDescription(sanitize(truncateAfterWords(req.description(), 500)));


        if (img != null && !img.isEmpty()) {// only update if img is there
            validateImageExtension(img.getOriginalFilename());

            boolean isCurrentlyDefaultImage = existing.getImageUrl() == null || existing.getImageUrl().equals(defaultImage);

            try {
                String imageUrl = isCurrentlyDefaultImage
                        ? uploadImageToR2(listingId, img) // just create and set in db
                        : replaceFile(existing.getImageUrl(), listingId, img); // actually replace the file (no undo)
                existing.setImageUrl(imageUrl);
            } catch (IOException | SdkException e) {
                log.warn("Image update failed for listing '{}', keeping previous image: {}", listingId, e.getMessage(), e);
            }
        }


        if (existing.getListingType().equalsIgnoreCase(ListingType.RENTAL.getValue())) {
            if (req.rentalPeriod() != null && !req.rentalPeriod().isEmpty()) {
                if (req.rentalPeriod().size() != 2) {
                    throw new IllegalArgumentException("Only 2 dates are allowed");
                }

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate start;
                LocalDate end;
                try {
                    start = LocalDate.parse(req.rentalPeriod().get(0), dateFormatter);
                    end = LocalDate.parse(req.rentalPeriod().get(1), dateFormatter);
                } catch (java.time.format.DateTimeParseException e) {
                    throw new IllegalArgumentException("Rental dates must be in yyyy-MM-dd format");
                }

                if(start.compareTo(LocalDate.now()) < 0){
                    throw new IllegalArgumentException("start date cannot be before today");
                }

                if(end.compareTo(start) < 0){
                    throw new IllegalArgumentException("end date cannot be before start date");
                }

                RentalPeriod borrowDate = new RentalPeriod();
                borrowDate.setStartDate(start);
                borrowDate.setEndDate(end);
                existing.setRentalPeriod(borrowDate);
            }
        }
        else{ //Sale listing
            existing.setRentalPeriod(null);// better to just always set it to null just incase
        }

        if (req.version() != null && !existing.getVersion().equals(req.version())) {
            existing.setVersion(sanitize(req.version()));
        }
        existing.setUpdatedAt(LocalDateTime.now());

        existing.setIsNegotiable(req.isNegotiable());

        String username = userRepository.findById(userId.toString())
        .orElseThrow(() -> new ResourceNotFound("User not found"))
        .getUsername();

        if(!existing.getUsername().equals(username)){
            existing.setUsername(username);
        }

        return mapToResponse(listingRepository.save(existing));
    }

    public List<ListingResponse> getUserListings(String token) {
        return listingRepository.findByUserId(jwtService.extractUserId(token))
        .stream().map(this::mapToResponse).toList();
    }

    private ListingResponse mapToResponse(Listing listing) {
        Optional<Boardgame> validGame = boardGameRepository.findByTitle(listing.getGameTitle());

        if (validGame.isEmpty()) {
            log.warn("Listing '{}' references game '{}' which no longer exists in boardGameRepository; returning empty genres", listing.getId(), listing.getGameTitle());
        }

        List<String> genres = validGame.map(Boardgame::getGenres)
                .filter(g -> g != null && !g.isEmpty())
                .orElseGet(ArrayList::new);

        return new ListingResponse(
                listing.getId(),
                listing.getListingTitle(),
                listing.getUsername(),
                listing.getUserId().toString(),
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
                genres,
                listing.getRentalPeriod(),
                listing.getStatus());
    }

}