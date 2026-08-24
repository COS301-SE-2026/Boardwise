package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boardwise.backend.user_service.enums.Visibility;

import lombok.Data;

@Document(collection = "GROUPS")
@Data
public class Group {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
    private String imageUrl;
    private String description;
    private String ownerId;
    private Visibility visibility;
    private String category;
    private Instant createdAt;

    public Group(String name, String imageUrl, String description, String category, String ownerId, Visibility visibility){
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.ownerId = ownerId;
        this.visibility = visibility;
        this.category = category;
        this.createdAt = Instant.now();
    }
}
