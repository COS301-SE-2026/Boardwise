package com.boardwise.backend.user_service.dtos.response;

import java.time.LocalDate;
import java.time.LocalTime;

import org.bson.types.ObjectId;

import com.boardwise.backend.user_service.dtos.LiveEventAttendeeList;
import com.boardwise.backend.user_service.enums.EventPrivacy;
import com.boardwise.backend.user_service.enums.LiveDurationCategory;
import com.boardwise.backend.user_service.enums.LiveEventType;
import com.boardwise.backend.user_service.enums.TableTone;

public record LiveEventResponseDTO(String id, String boardgameId, String hostId, String title,LiveEventType type, String venueName, String table,String link, LocalDate date, LocalTime time,LiveDurationCategory duration, Integer maxSeats, TableTone tone, EventPrivacy eventPrivacy, Boolean automaticApproval, LiveEventAttendeeList attendees){
    
}