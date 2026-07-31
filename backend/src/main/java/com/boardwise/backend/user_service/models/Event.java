package com.boardwise.backend.user_service.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "EVENTS")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    
    @Id
    private String id;
    private String name;
    private String description;
    private String eventImg;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String locationText;
    
    // Possible addition of attendee cap
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;
    private Visibility visibility;
    private EventStatus status;
    private String creatorId;
    private List<String> games; // store the games that'll be played
    private Instant createdAt;

    public Event(
        String name,
        String description,
        String eventImg,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String locationText,
        GeoJsonPoint location,
        Visibility visibility,
        String creatorId,
        List<String> games
    ){
        this.name = name;
        this.description = description;
        this.eventImg = eventImg;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.locationText = locationText;
        this.location = location;
        this.visibility = visibility;
        this.status = EventStatus.OPEN;
        this.creatorId = creatorId;
        this.games = games;
        this.createdAt = Instant.now();
    }
}
