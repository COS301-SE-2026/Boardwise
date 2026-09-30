package com.boardwise.backend.user_service.models;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Document("LIVE_EVENT_MESSAGES")
@CompoundIndex(def = "{'eventId': 1, 'createdAt': 1}")
public record LiveEventMessage(
    @Id @JsonSerialize(using = ToStringSerializer.class) ObjectId id,
    String eventId,
    String senderId,
    String senderUsername,
    String content,
    boolean isHost,
    Instant createdAt
) {}