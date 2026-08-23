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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.DeRsvpDTO;
import com.boardwise.backend.user_service.dtos.EventDTO;
import com.boardwise.backend.user_service.dtos.EventHostInfo;
import com.boardwise.backend.user_service.dtos.EventInfoDTO;
import com.boardwise.backend.user_service.dtos.EventInviteDTO;
import com.boardwise.backend.user_service.dtos.EventInviteInfo;
import com.boardwise.backend.user_service.dtos.EventUpdateDTO;
import com.boardwise.backend.user_service.dtos.GameInventoryDTO;
import com.boardwise.backend.user_service.dtos.InviteDTO;
import com.boardwise.backend.user_service.dtos.InviteNotification;
import com.boardwise.backend.user_service.enums.EventStatus;
import com.boardwise.backend.user_service.enums.RSVPStatus;
import com.boardwise.backend.user_service.enums.Visibility;
import com.boardwise.backend.user_service.models.Boardgame;
import com.boardwise.backend.user_service.models.Event;
import com.boardwise.backend.user_service.models.EventAttendee;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.BoardGameRepository;
import com.boardwise.backend.user_service.repository.EventAttendeeRepository;
import com.boardwise.backend.user_service.repository.EventRepository;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CommunityService {

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final BoardGameRepository gameRepo;
    private final JWTService jwtService;
    private final GeoApiContext geoApiContext;
    private final R2StorageService bucket;
    private final EventAttendeeRepository eaRepo;
    private final NotificationService notifService;
    private final MongoTemplate template;

    public Map<String, Object> getEvents(String token, String name, Integer pageNumber) {
        User user = getUserFromToken(token);
        Map<String, Object> result = new HashMap<>();
        Pageable page;
        List<Event> dbEvents;
        List<EventDTO> events = new ArrayList<>();
        String message;
        
        if(name == null){
            int pageIdx = pageNumber == null ? 0 : (pageNumber - 1);
            page = PageRequest.of(pageIdx, 10);
            dbEvents = eventRepo.findAll(page).getContent();
            message = "Events successfully retrieved.";            
        }
        else{
            String cleanName = AuthService.sanitize(name);
            Criteria searchCriteria = Criteria.where("name").regex(cleanName, "i");
            page = PageRequest.of(0,10);
            Query query = new Query(searchCriteria);
            query.with(page);
            dbEvents = template.find(query, Event.class);
            message = "Queried event(s) successfully retrieved.";
        }

        for(Event event : dbEvents){
            List<Boardgame> eventGames = gameRepo.findAllById(event.getGames());
            User host = userRepo.findById(event.getCreatorId()).get();

            EventAttendee forExample = new EventAttendee();
            forExample.setEventId(event.getId());
            forExample.setStatus(RSVPStatus.ATTENDING);
            Example<EventAttendee> example = Example.of(forExample);
            int attendeeCount = (int) eaRepo.count(example);

            Optional<EventAttendee> ea = eaRepo.findByUserIdAndEventId(user.getId(), event.getId());
            boolean attending = ea.isPresent() && ea.get().getStatus() == RSVPStatus.ATTENDING;

            boolean isHost = event.getCreatorId().equals(user.getId());
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

            if(event.getStatus() == EventStatus.OPEN){
                RSVPStatus status = (attending || event.getCreatorId().equals(user.getId()))? 
                                    RSVPStatus.ATTENDING : 
                                    RSVPStatus.NOT_ATTENDING;

                events.add(EventDTO.fromEntity(
                    event, attendeeCount, status, hostInfo, isHost, games
                ));
            }

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

        EventAttendee newAttendee = new EventAttendee(
            user.getId(), newEvent.getId(), RSVPStatus.ATTENDING
        );
        eaRepo.save(newAttendee); // save creator to attendee collection

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

        EventDTO data = EventDTO.fromEntity(newEvent, 1, RSVPStatus.ATTENDING, hostInfo, true, games);
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
            // TODO: notify attendees about the update
            event = eventRepo.save(event);

            EventAttendee forExample = new EventAttendee();
            forExample.setEventId(event.getId());
            forExample.setStatus(RSVPStatus.ATTENDING);
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
            
            
            EventDTO data = EventDTO.fromEntity(event, attendeeCount, RSVPStatus.ATTENDING, hostInfo, true, games);
            result.put("message", "Event successfully updated.");
            result.put("data", data);
        }
        else{
            result.put("message", "Event was not updated. No new data was provided.");
        }    

        return result;
    }

    public Map<String, Object> cancelEvent(String token, String eventId) throws NoSuchElementException, IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();
        Optional<Event> event = eventRepo.findById(eventId);

        if(!userRepo.existsById(userId))
            throw new NoSuchElementException("User with ID: " + userId + " does not exist.");
        else if(event.isEmpty())
            throw new NoSuchElementException("Event with ID: " + eventId + " does not exist.");
        else if(!event.get().getCreatorId().equals(userId))
            throw new IllegalAccessException("User with ID: " + userId + " is not the host of this event.");
        
        Map<String, String> eventInfo = new HashMap<>();
        User host = userRepo.findById(event.get().getCreatorId()).get();
        eventInfo.put("eventName", event.get().getName());
        eventInfo.put("eventHost", host.getUsername());

        // TODO: might need to add notifications here

        // delete recorded attendees
        eaRepo.deleteByEventId(eventId);

        // delete actual event
        event.get().setStatus(EventStatus.CANCELLED);
        eventRepo.save(event.get());

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
        forExample.setStatus(RSVPStatus.ATTENDING);
        Example<EventAttendee> example = Example.of(forExample);
        int attendeeCount = ((int) eaRepo.count(example));
        
        User host = userRepo.findById(event.getCreatorId()).get();
        boolean isHost = event.getCreatorId().equals(user.getId());
        EventHostInfo hostInfo = new EventHostInfo(host.getUsername(), host.getProfilePicture());

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

        EventDTO data = EventDTO.fromEntity(event, attendeeCount, RSVPStatus.ATTENDING, hostInfo, isHost, games);


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

        Optional<EventAttendee> changed = eaRepo.findByUserIdAndEventId(user.getId(), event.getId());

        if(changed.isEmpty()){
            throw new IllegalAccessException("User has not RSVP'd for this event.");
        }

        changed.get().setStatus(RSVPStatus.NOT_ATTENDING);
        eaRepo.save(changed.get());

        EventAttendee forExample = new EventAttendee();
        forExample.setEventId(dto.eventId());
        forExample.setStatus(RSVPStatus.ATTENDING);
        Example<EventAttendee> example = Example.of(forExample);
        int attendeeCount = ((int) eaRepo.count(example));
        
        User host = userRepo.findById(event.getCreatorId()).get();
        boolean isHost = event.getCreatorId().equals(user.getId());
        EventHostInfo hostInfo = new EventHostInfo(host.getUsername(), host.getProfilePicture());

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

        EventDTO data = EventDTO.fromEntity(event, attendeeCount, RSVPStatus.NOT_ATTENDING, hostInfo, isHost, games);

        result.put("message", "User attendance successfully removed.");
        result.put("data", data);

        return result;
    }

    public Map<String, Object> inviteToEvent(String token, EventInviteDTO inviteInfo) throws NoSuchElementException{
        User inviter = getUserFromToken(token);
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
            invitee.get().getId(), inviteInfo.eventId(), RSVPStatus.INVITED 
        );
        eaRepo.save(newAttendee);

        Event event = eventRepo.findById(inviteInfo.eventId()).get();        
        EventInviteInfo invite = new EventInviteInfo(
            event.getId(),
            event.getName(),
            event.getEventImg(),
            event.getStartDateTime().toLocalDate()
        ); 
        EventHostInfo sender = new EventHostInfo(inviter.getUsername(), inviter.getProfilePicture());                                   
        InviteNotification payload = new InviteNotification(sender, invite);

        notifService.send(
            invitee.get().getId(), 
            payload
        );

        result.put("message", "Invite successfully sent.");
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
            case "accept" -> RSVPStatus.ATTENDING;
            case "decline" -> RSVPStatus.NOT_ATTENDING;
            default -> throw new IllegalArgumentException("Invalid invite response status provided.");
        };

        EventAttendee forExample = new EventAttendee();
        forExample.setEventId(eventId);
        forExample.setUserId(userId);
        Example<EventAttendee> example = Example.of(forExample);
        Optional<EventAttendee> ea = eaRepo.findOne(example);

        if(ea.isEmpty())
            throw new NoSuchElementException("The invite you are trying to respond to does not exist");
        else if(ea.get().getStatus() != RSVPStatus.INVITED){
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

    public Map<String, Object> getUserInvitations(String token){
        Map<String, Object> result = new HashMap<>();
        User user = getUserFromToken(token);
        List<EventAttendee> invites = eaRepo.findAllByUserIdAndStatus(user.getId(), RSVPStatus.INVITED);

        int count = invites.size();
        List<InviteDTO> dtos = new ArrayList<>();
        for(EventAttendee invite : invites){
            Event event = eventRepo.findById(invite.getEventId()).get();
            if(event.getStatus() == EventStatus.CANCELLED)
                continue;

            User host = userRepo.findById(event.getCreatorId()).get();
            EventHostInfo hostInfo = new EventHostInfo(host.getUsername(), 
                                                host.getProfilePicture());
            EventInviteInfo eventInfo = new EventInviteInfo(
                                            event.getId(), 
                                            event.getName(), 
                                            event.getEventImg(), 
                                            event.getStartDateTime().toLocalDate()
                                        );

            InviteDTO dto = new InviteDTO(
                invite.getStatus(),
                hostInfo,
                eventInfo
            );
            
            dtos.add(dto);
        }
        
        result.put("message", "User invites successfully retrieved.");
        result.put("inviteCount", count);
        result.put("invites", dtos);

        return result;
    }

    public Map<String, Object> getEvent(String token, String eventId) {
        User currentUser = getUserFromToken(token);
        Optional<Event> opEvent = eventRepo.findById(eventId);
        Map<String, Object> result = new HashMap<>();

        if(opEvent.isEmpty())
            throw new NoSuchElementException("No event associated with ID: " + eventId + " exists");

        Event event = opEvent.get();
        EventAttendee forExample = new EventAttendee();
        forExample.setEventId(event.getId());
        forExample.setStatus(RSVPStatus.ATTENDING);
        Example<EventAttendee> example = Example.of(forExample);
        int attendeeCount = (int) eaRepo.count(example);

        Optional<EventAttendee> ea = eaRepo.findByUserIdAndEventId(currentUser.getId(), eventId);
        RSVPStatus userStatus;
        if(ea.isEmpty())
            userStatus = RSVPStatus.NOT_ATTENDING;
        else 
            userStatus = ea.get().getStatus();

        User host = userRepo.findById(event.getCreatorId()).get();
        boolean isHost = event.getCreatorId().equals(currentUser.getId());
        EventHostInfo hostInfo = new EventHostInfo(
            host.getUsername(), host.getProfilePicture()
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
        
        
        EventDTO data = EventDTO.fromEntity(event, attendeeCount, userStatus, hostInfo, isHost, games);
        result.put("message", "Event successfully retrieved.");
        result.put("data", data);

        return result;
    }

    private User getUserFromToken(String token){
        String userId = jwtService.extractUserId(token).toString();
        return userRepo.findById(userId).get();
    }
}
