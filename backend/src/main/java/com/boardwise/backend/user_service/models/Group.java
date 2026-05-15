package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Document(collection = "Groups")
@Data
public class Group {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
    private String description;
    @Field("owner_id")
    private String ownerId;
    private String visibility;
    private Instant createdAt;

    public Group(String name, String description, String ownerId, String visibility){
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.visibility = visibility;
        this.createdAt = Instant.now();
    }
}
