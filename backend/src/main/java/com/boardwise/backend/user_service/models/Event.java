package com.boardwise.backend.user_service.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "Events")
@Data
public class Event {
    
    @Id
    private String id;
    
}
