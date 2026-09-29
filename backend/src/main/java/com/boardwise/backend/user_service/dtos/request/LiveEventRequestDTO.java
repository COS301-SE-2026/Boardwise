package com.boardwise.backend.user_service.dtos.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.bson.types.ObjectId;

import com.boardwise.backend.user_service.enums.EventPrivacy;
import com.boardwise.backend.user_service.enums.LiveDurationCategory;
import com.boardwise.backend.user_service.enums.LiveEventType;
import com.boardwise.backend.user_service.enums.TableTone;
import com.boardwise.backend.user_service.dtos.LiveEventAttendee;

//Data from the form
public record LiveEventRequestDTO(    
    ObjectId boardgameId, // boardgame Id
    ObjectId hostId,
    String title,
    LiveEventType type,
    String venueName,
    String table,
    String link,
    LocalDate date, // scheduled date
    LocalTime time,  
    LiveDurationCategory duration,
    Integer maxSeats, // incl. host
    TableTone tone,
    EventPrivacy privacy,
    Boolean automaticApproval,
    List<LiveEventAttendee> liveAttendees
    ) {
}
