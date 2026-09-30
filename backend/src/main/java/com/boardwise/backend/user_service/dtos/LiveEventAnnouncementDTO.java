package com.boardwise.backend.user_service.dtos;

import java.time.Instant;

import org.bson.types.ObjectId;

public record LiveEventAnnouncementDTO( 
    ObjectId id,   
    String senderId,
    String eventId,
    String message,
    Instant sentAt) {
}