package com.boardwise.backend.user_service.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "USER")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class User {
    
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String emailAddress;

    private String password;
    private String firstName;
    private String lastName;
    private String profilePicture; // image url from cloud buckets
    private String location;
    private Preferences preferences;
    private Instant createdAt;
    private List<String> ownedGames;

    public User(String username, String firstName, String lastName, 
        String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.preferences = new Preferences();
        this.emailAddress = email;
        this.ownedGames = new ArrayList<>();
        this.createdAt = Instant.now();
        this.profilePicture = null;
        this.location = null;
    }

}
