package com.boardwise.backend.user_service.models;

import java.time.LocalDate;
import java.time.LocalTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boardwise.backend.user_service.dtos.LiveEventAttendeeList;
import com.boardwise.backend.user_service.enums.EventPrivacy;
import com.boardwise.backend.user_service.enums.LiveDurationCategory;
import com.boardwise.backend.user_service.enums.LiveEventType;
import com.boardwise.backend.user_service.enums.TableTone;
import com.mongodb.lang.Nullable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "LIVE_EVENTS")

@Builder 
@AllArgsConstructor 
@Data
public class LiveEvent {
    @Id
    @NotNull 
    String id;
    @NotNull
    ObjectId boardgameId; // boardgame Id
    @NotNull
    ObjectId hostId;
    @NotBlank 
    String title;
    @NotNull
    LiveEventType type;
    @NotNull
    String venueName;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    GeoJsonPoint location;
    @NotBlank 
    String table;
    @Nullable String link;
    @Nullable LocalDate date; // scheduled date
    @Nullable LocalTime time;
    @NotNull  
    LiveDurationCategory duration;
    @NotNull 
    Integer maxSeats; // incl. host
    @NotNull 
    TableTone tone;
    @NotNull 
    EventPrivacy privacy;
    @NotNull
    Boolean automaticApproval;
    LiveEventAttendeeList liveAttendees; 
}
