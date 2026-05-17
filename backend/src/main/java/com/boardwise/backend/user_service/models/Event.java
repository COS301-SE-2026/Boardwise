package com.boardwise.backend.user_service.models;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "Events")
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

    @Field("creator_id")
    private String creatorId;

    private List<String> games; // store the games that'll be played

    @Field("created_at")
    private Instant createdAt;

    
}
