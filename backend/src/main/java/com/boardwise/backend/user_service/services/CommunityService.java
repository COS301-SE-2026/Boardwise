package com.boardwise.backend.user_service.services;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.DeRsvpDTO;
import com.boardwise.backend.user_service.dtos.EventDTO;
import com.boardwise.backend.user_service.dtos.EventHostInfo;
import com.boardwise.backend.user_service.dtos.EventInfoDTO;
import com.boardwise.backend.user_service.dtos.EventInviteDTO;
import com.boardwise.backend.user_service.dtos.EventUpdateDTO;
import com.boardwise.backend.user_service.dtos.GameInventoryDTO;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.Event;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.EventsRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

// import ch.qos.logback.classic.pattern.DateConverter;

@Service
public class CommunityService {

    private final EventsRepository eventRepo;
    private final UserRepository userRepo;
    private final BoardGameRepository gameRepo;
    private final JWTService jwtService;
    private final GeoApiContext geoApiContext;
    private final R2StorageService bucket;

    CommunityService(
        EventsRepository eventRepo, 
        UserRepository userRepo, 
        BoardGameRepository gameRepo,
        JWTService jwtService, 
        GeoApiContext geoApiContext,
        R2StorageService bucket
    ){
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.gameRepo = gameRepo;
        this.jwtService = jwtService;
        this.geoApiContext = geoApiContext;
        this.bucket = bucket;
    }

    public Map<String, Object> getEvents() {
        Map<String, Object> result = new HashMap<>();
        Pageable page = PageRequest.of(0, 25);
        List<Event> events = eventRepo.findAll(page).getContent();

        // TODO: use the EventDTO instead of the model

        result.put("message", "Events successfully retrieved");
        result.put("events", events);
        return result;
    }

    public Map<String, Object> createEvent(String token, EventInfoDTO eventInfo, MultipartFile eventImg) throws ApiException, InterruptedException, IllegalArgumentException, IOException {
        Map<String, Object> result = new HashMap<>();
        User user = getUserFromToken(token);

        // Sanitise fn
        String eventName = AuthService.sanitize(eventInfo.name());
        String eventDesc = AuthService.sanitize(eventInfo.description());
        String eventLocationText = AuthService.sanitize(eventInfo.location());
        LocalDateTime eventStart = LocalDateTime.of(eventInfo.date(), eventInfo.startTime());
        LocalDateTime eventEnd = eventInfo.endTime().isBefore(eventInfo.startTime()) ?
                                        LocalDateTime.of(
                                            eventInfo.date().plusDays(1L), 
                                            eventInfo.endTime()
                                        ) :
                                        LocalDateTime.of(
                                            eventInfo.date(), 
                                            eventInfo.endTime()
                                        );
        
        List<String> eventGames = new ArrayList<>();
        for(String game : eventInfo.games()){
            String id = AuthService.sanitize(game);
            eventGames.add(id);
        }

        GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, eventLocationText).await();

        if(results.length == 0)
            throw new IllegalArgumentException("Could not find coordinates for location: " + eventLocationText);

        double latitude = results[0].geometry.location.lat;
        double longitude = results[0].geometry.location.lng;

        GeoJsonPoint point = new GeoJsonPoint(new Point(longitude, latitude));

        Event newEvent = new Event(
            eventName,
            eventDesc,
            null,
            eventStart,
            eventEnd,
            eventLocationText,
            point,
            eventInfo.visibility(),
            user.getId(),
            eventGames
        );
        
        newEvent = eventRepo.save(newEvent);
        String fileName = bucket.uploadFile(eventImg, newEvent.getId());
        String imageUrl = bucket.getFileUrl(fileName);
        newEvent.setEventImg(imageUrl);
        newEvent = eventRepo.save(newEvent);

        EventHostInfo hostInfo = new EventHostInfo(user.getUsername(), user.getProfilePicture());

        List<GameInventoryDTO> games = new ArrayList<>();
        for(String gameId : newEvent.getGames()){
            Boardgame game = gameRepo.findById(gameId).get();
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(), 
                game.getTitle(), 
                game.getDescription(), 
                game.getImageURL(), 
                game.getGenres()
            );
            games.add(dto);
        }

        EventDTO data = EventDTO.fromEntity(newEvent, hostInfo, games);
        result.put("message", "Event successfully created.");
        result.put("data", data);

        return result;
    }

    public Map<String, Object> updateEvent(String token, EventUpdateDTO newInfo, MultipartFile newImage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEvent'");
    }

    public Map<String, Object> rsvp(String token, String eventId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rsvp'");
    }

    public Map<String, Object> deRsvp(String token, DeRsvpDTO dto) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deRsvp'");
    }

    public Map<String, Object> inviteToEvent(String token, EventInviteDTO inviteInfo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'inviteToEvent'");
    }

    public Map<String, Object> deleteEvent(String token, String eventId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteEvent'");
    }

    private User getUserFromToken(String token){
        String userId = jwtService.extractUserId(token).toString();
        return userRepo.findById(userId).get();
    }
}
