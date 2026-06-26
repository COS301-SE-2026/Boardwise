package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;

@Document(collection = "GROUPS")
@Data
public class Group {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
    private String description;
    private String ownerId;
    private String visibility;
    private String category;
    private Instant createdAt;

    public Group(String name, String description, String category, String ownerId, String visibility){
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.visibility = visibility;
        this.category = category;
        this.createdAt = Instant.now();
    }
}
