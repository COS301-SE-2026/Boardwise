package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "EVENT_PARTICIPANTS")
@CompoundIndex(def = "{'eventId': 1, 'userId': 1}", unique = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventAttendee {
    @Id
    private String id;
    private String userId;
    private String eventId;
    private RSVPStatus status;
    private Instant respondedAt;

    public EventAttendee(
        String userId,
        String eventId,
        RSVPStatus status
    ){
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
        this.respondedAt = Instant.now();
    }
}
