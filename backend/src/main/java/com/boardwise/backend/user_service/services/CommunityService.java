package com.boardwise.backend.user_service.services;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.TextCriteria;
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
import com.boardwise.backend.user_service.models.EventAttendee;
import com.boardwise.backend.user_service.models.RSVPStatus;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.models.Visibility;
import com.boardwise.backend.user_service.repos.BoardGameRepository;
import com.boardwise.backend.user_service.repos.EventAttendeeRepository;
import com.boardwise.backend.user_service.repos.EventsRepository;
import com.boardwise.backend.user_service.repos.UserRepository;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;


@Service
public class CommunityService {

    private final EventsRepository eventRepo;
    private final UserRepository userRepo;
    private final BoardGameRepository gameRepo;
    private final JWTService jwtService;
    private final GeoApiContext geoApiContext;
    private final R2StorageService bucket;
    private final EventAttendeeRepository eaRepo;

    CommunityService(
        EventsRepository eventRepo, 
        UserRepository userRepo, 
        BoardGameRepository gameRepo,
        EventAttendeeRepository eaRepo,
        JWTService jwtService, 
        GeoApiContext geoApiContext,
        R2StorageService bucket
    ){
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.gameRepo = gameRepo;
        this.eaRepo = eaRepo;
        this.jwtService = jwtService;
        this.geoApiContext = geoApiContext;
        this.bucket = bucket;
    }

    public Map<String, Object> getEvents(String name) {
        Map<String, Object> result = new HashMap<>();
        Pageable page;
        List<Event> dbEvents;
        List<EventDTO> events = new ArrayList<>();
        String message;
        
        if(name == null){
            page = PageRequest.of(0, 25);
            dbEvents = eventRepo.findAll(page).getContent();
            message = "Events successfully retrieved.";            
        }
        else{
            page = PageRequest.of(0, 10);
            TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(name);
            dbEvents = eventRepo.findAllBy(criteria, page);
            message = "Queried event(s) successfully retrieved.";
        }

        for(Event event : dbEvents){
            List<Boardgame> eventGames = gameRepo.findAllById(event.getGames());
            User host = userRepo.findById(event.getCreatorId()).get();

            EventAttendee forExample = new EventAttendee();
            forExample.setEventId(event.getId());
            Example<EventAttendee> example = Example.of(forExample);
            int attendeeCount = (int) eaRepo.count(example);

            EventHostInfo hostInfo = new EventHostInfo(
                host.getUsername(),
                host.getProfilePicture()
            );
            List<GameInventoryDTO> games = new ArrayList<>();
            for(Boardgame game : eventGames){
                GameInventoryDTO dto = new GameInventoryDTO(
                    game.getId(), 
                    game.getTitle(), 
                    game.getDescription(), 
                    game.getImageURL(), 
                    game.getGenres()
                );
                games.add(dto);
            }

            events.add(EventDTO.fromEntity(event, attendeeCount, hostInfo, games));
        }
        result.put("message", message);
        result.put("result", events);
        return result;
    }

    public Map<String, Object> createEvent(String token, EventInfoDTO eventInfo, MultipartFile eventImg) throws ApiException, InterruptedException, NoSuchElementException, IOException {
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
            throw new NoSuchElementException("Could not find coordinates for location: " + eventLocationText);

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

        List<Boardgame> dbGames = gameRepo.findAllById(newEvent.getGames());
        List<GameInventoryDTO> games = new ArrayList<>();
        for(Boardgame game : dbGames){
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(), 
                game.getTitle(), 
                game.getDescription(), 
                game.getImageURL(), 
                game.getGenres()
            );
            games.add(dto);
        }

        EventDTO data = EventDTO.fromEntity(newEvent, 1, hostInfo, games);
        result.put("message", "Event successfully created.");
        result.put("data", data);

        return result;
    }

    public Map<String, Object> updateEvent(String token, String eventId, EventUpdateDTO newInfo, MultipartFile newImage) throws IllegalAccessException, IllegalArgumentException, NoSuchElementException, IOException, ApiException, InterruptedException {
        User user = getUserFromToken(token);
        Optional<Event> preEvent = eventRepo.findById(eventId);
        boolean eventChanged = false;
        Map<String, Object> result = new HashMap<>();

        if(preEvent.isEmpty())
            throw new NoSuchElementException("Event with ID: " + eventId + " does not exist.");
        
        Event event = preEvent.get();
        if(!event.getCreatorId().equals(user.getId()))
            throw new IllegalAccessException("This user is not the host of this event.");

        if(newImage != null){
            eventChanged = true;
            String fileName = bucket.uploadFile(newImage, eventId);
            String imageUrl = bucket.getFileUrl(fileName);
            event.setEventImg(imageUrl);
        }

        if(newInfo != null){
            String newName = AuthService.sanitize(newInfo.name());
            String newDesc = AuthService.sanitize(newInfo.description());
            String newLocation = AuthService.sanitize(newInfo.location());
            LocalDate newDate = newInfo.date();
            LocalTime newStart = newInfo.startTime();
            LocalTime newEnd = newInfo.endTime();
            Visibility newVisibility = newInfo.visibility();
            List<String> newGames = new ArrayList<>();
            if(newInfo.games() != null){
                for(String game : newInfo.games()){
                    String id = AuthService.sanitize(game);
                    newGames.add(id);
                }
            }
            


            if(newName != null && !event.getName().equals(newName)){
                eventChanged = true;
                event.setName(newName);
            }
            if(newDesc != null && !event.getDescription().equals(newDesc)){
                eventChanged = true;
                event.setDescription(newDesc);
            }
            if(newLocation != null && !event.getLocationText().equals(newLocation)){
                eventChanged = true;
                event.setLocationText(newLocation);

                GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, newLocation).await();
                if(results.length == 0)
                    throw new NoSuchElementException("Could not find coordinates for location: " + newLocation);

                double latitude = results[0].geometry.location.lat;
                double longitude = results[0].geometry.location.lng;

                GeoJsonPoint point = new GeoJsonPoint(new Point(longitude, latitude));
                event.setLocation(point);
            }
            if(newDate != null && !event.getStartDateTime().toLocalDate().equals(newDate)){
                eventChanged = true;
                LocalDateTime startDateTime = LocalDateTime.of(
                    newDate, 
                    event.getStartDateTime().toLocalTime()
                );

                boolean ifPlusOne = event.getStartDateTime().toLocalDate()
                                    .isBefore(event.getEndDateTime().toLocalDate());

                LocalDateTime endDateTime = ifPlusOne ? 
                                            LocalDateTime.of(newDate.plusDays(1), event.getEndDateTime().toLocalTime()) :
                                            LocalDateTime.of(newDate, event.getEndDateTime().toLocalTime());

                event.setStartDateTime(startDateTime);
                event.setEndDateTime(endDateTime);
            }
            if(newStart != null && !event.getStartDateTime().toLocalTime().equals(newStart)){
                LocalDateTime startDateTime = LocalDateTime.of(
                    event.getStartDateTime().toLocalDate(), newStart
                );

                if(startDateTime.isAfter(event.getEndDateTime()) || startDateTime.isEqual(event.getEndDateTime()))
                    throw new IllegalArgumentException("Event start time must be before end time.");

                event.setStartDateTime(startDateTime);
                eventChanged = true;
                
            }
            if(newEnd != null && !event.getEndDateTime().toLocalTime().equals(newEnd)){
                LocalDateTime endDateTime = LocalDateTime.of(
                    event.getEndDateTime().toLocalDate(), newEnd
                );

                if(endDateTime.isBefore(event.getStartDateTime()) || endDateTime.isEqual(event.getStartDateTime()))
                    throw new IllegalArgumentException("Event end time must be after start time.");

                event.setEndDateTime(endDateTime);
                eventChanged = true;
                
            }
            if(newVisibility != null && event.getVisibility() != newVisibility){
                eventChanged = true;
                event.setVisibility(newVisibility);
            }
            if(newGames.size() != 0){
                Set<String> newGameSet = new HashSet<>(newGames);
                Set<String> oldGameSet = new HashSet<>(event.getGames());

                if(!oldGameSet.equals(newGameSet)){
                    eventChanged = true;
                    event.setGames(newGames);
                }
            }
        }
        
        if(eventChanged){
            event = eventRepo.save(event);

            EventAttendee forExample = new EventAttendee();
            forExample.setEventId(event.getId());
            Example<EventAttendee> example = Example.of(forExample);
            int attendeeCount = (int) eaRepo.count(example);

            EventHostInfo hostInfo = new EventHostInfo(
                user.getUsername(), user.getProfilePicture()
            );

            List<Boardgame> dbGames = gameRepo.findAllById(event.getGames());
            List<GameInventoryDTO> games = new ArrayList<>();
            for(Boardgame game : dbGames){
                GameInventoryDTO dto = new GameInventoryDTO(
                    game.getId(), 
                    game.getTitle(), 
                    game.getDescription(), 
                    game.getImageURL(), 
                    game.getGenres()
                );
                games.add(dto);
            }
            
            
            EventDTO data = EventDTO.fromEntity(event, attendeeCount, hostInfo, games);
            result.put("message", "Event successfully updated.");
            result.put("data", data);
        }
        else{
            result.put("message", "Event was not updated. No new data was provided.");
        }    

        return result;
    }

    public Map<String, Object> deleteEvent(String token, String eventId) throws NoSuchElementException, IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();
        Optional<Event> event = eventRepo.findById(eventId);

        if(!userRepo.existsById(userId))
            throw new NoSuchElementException("User with ID: " + userId + " does not exist.");
        else if(event.isEmpty())
            throw new NoSuchElementException("Event with ID: " + eventId + " does not exist.");
        else if(!event.get().getCreatorId().equals(userId))
            throw new IllegalAccessException("User with ID: " + userId + " is not the host of this event.");

        // delete recorded attendees
        eaRepo.deleteByEventId(eventId);
        
        // delete actual event
        eventRepo.deleteById(eventId);

        result.put("message", "Event successfully deleted.");
        return result;
    }

    public Map<String, Object> rsvp(String token, String eventId) throws NoSuchElementException{
        User user = getUserFromToken(token);
        Event event = eventRepo.findById(eventId).get();
        Map<String, Object> result = new HashMap<>();

        if(event == null)
            throw new NoSuchElementException("Event with ID: " + eventId + " does not exist.");

        EventAttendee newAttendee = new EventAttendee(
            user.getId(), eventId, RSVPStatus.ATTENDING
        );
        
        newAttendee = eaRepo.save(newAttendee);
        EventAttendee forExample = new EventAttendee();
        forExample.setEventId(eventId);
        Example<EventAttendee> example = Example.of(forExample);
        int attendeeCount = ((int) eaRepo.count(example)) + 1;
        
        EventHostInfo hostInfo = new EventHostInfo(user.getUsername(), user.getProfilePicture());

        List<Boardgame> dbGames = gameRepo.findAllById(event.getGames());
        List<GameInventoryDTO> games = new ArrayList<>();
        for(Boardgame game : dbGames){
            GameInventoryDTO dto = new GameInventoryDTO(
                game.getId(), 
                game.getTitle(), 
                game.getDescription(), 
                game.getImageURL(), 
                game.getGenres()
            );
            games.add(dto);
        }

        EventDTO data = EventDTO.fromEntity(event, attendeeCount, hostInfo, games);


        result.put("message", "User attendance successfully recorded.");
        result.put("data", data);

        return result;
    }

    public Map<String, Object> deRsvp(String token, DeRsvpDTO dto) throws IllegalAccessException, NoSuchElementException {
        User user = getUserFromToken(token);
        Event event = eventRepo.findById(dto.eventId()).get();
        Map<String, Object> result = new HashMap<>();

        if(event == null)
            throw new NoSuchElementException("Event with ID: " + dto.eventId() + " does not exist.");

        Optional<EventAttendee> deleted = eaRepo.deleteByUserIdAndEventId(user.getId(), event.getId());

        if(deleted.isEmpty()){
            throw new IllegalAccessException("User has not RSVP'd for this event.");
        }

        EventAttendee forExample = new EventAttendee();
        forExample.setEventId(dto.eventId());
        Example<EventAttendee> example = Example.of(forExample);
        int attendeeCount = ((int) eaRepo.count(example)) + 1;
        
        EventHostInfo hostInfo = new EventHostInfo(user.getUsername(), user.getProfilePicture());

        List<Boardgame> dbGames = gameRepo.findAllById(event.getGames());
        List<GameInventoryDTO> games = new ArrayList<>();
        for(Boardgame game : dbGames){
            GameInventoryDTO gDto = new GameInventoryDTO(
                game.getId(), 
                game.getTitle(), 
                game.getDescription(), 
                game.getImageURL(), 
                game.getGenres()
            );
            games.add(gDto);
        }

        EventDTO data = EventDTO.fromEntity(event, attendeeCount, hostInfo, games);

        result.put("message", "User attendance successfully removed.");
        result.put("data", data);

        return result;
    }

    public Map<String, Object> inviteToEvent(String token, EventInviteDTO inviteInfo) throws NoSuchElementException{
        // User inviter = getUserFromToken(token);
        Optional<User> invitee = userRepo.findByUsername(inviteInfo.invitee());
        Map<String, Object> result = new HashMap<>();
        
        if(invitee.isEmpty())
            throw new NoSuchElementException(
                "Failed to send invite. User with username: " + 
                inviteInfo.invitee() +
                " does not exist."
            );
        else if(!eventRepo.existsById(inviteInfo.eventId()))
            throw new NoSuchElementException(
                "Failed to send invite. Event with ID: " + 
                inviteInfo.eventId() +
                " does not exist."
            );

        EventAttendee newAttendee = new EventAttendee(
            invitee.get().getId(), inviteInfo.eventId(), RSVPStatus.PENDING 
        );
        eaRepo.save(newAttendee);

        // TODO: send invite to the invitee (will require websockets)

        return result;
    }

    public Map<String, Object> respondToInvite(String token, String eventId, String status) throws NoSuchElementException, IllegalArgumentException{
        Map<String, Object> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();
        String response = AuthService.sanitize(status);
        

        if(!eventRepo.existsById(eventId))
            throw new NoSuchElementException(
                "Failed to send invite. Event with ID: " + 
                eventId +
                " does not exist."
            );
        
        RSVPStatus rsvp = switch (response.toLowerCase()) {
            case "attending" -> RSVPStatus.ATTENDING;
            case "not attending" -> RSVPStatus.NOT_ATTENDING;
            default -> throw new IllegalArgumentException("Invalid invite response status provided.");
        };

        EventAttendee forExample = new EventAttendee();
        forExample.setEventId(eventId);
        forExample.setUserId(userId);
        Example<EventAttendee> example = Example.of(forExample);
        Optional<EventAttendee> ea = eaRepo.findOne(example);

        if(ea.isEmpty())
            throw new NoSuchElementException("The invite you are trying to respond to does not exist");
        else if(ea.get().getStatus() != RSVPStatus.PENDING){
            Instant resStamp = ea.get().getRespondedAt();
            throw new IllegalArgumentException("User invite has already been responded to. Responded at: " + resStamp);
        }
            

        EventAttendee attendee = ea.get();
        attendee.setStatus(rsvp);
        attendee.setRespondedAt(Instant.now());
        eaRepo.save(attendee);

        result.put("message", "Invite response successfully recorded.");
        return result;
    }

    private User getUserFromToken(String token){
        String userId = jwtService.extractUserId(token).toString();
        return userRepo.findById(userId).get();
    }
}
