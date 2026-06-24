package com.boardwise.backend.user_service.models;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "EVENTS")
@Data
public class Event {
    
    @Id
    private String id;

    private String name;

    private String description;

    private Date date;

    private String time;

    private String location;

    private String visibility;

    private String creatorId;

    private List<String> games; // store the games that'll be played

    private Instant createdAt;

    
}
