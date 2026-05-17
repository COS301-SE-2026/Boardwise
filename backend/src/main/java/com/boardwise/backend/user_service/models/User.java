package com.boardwise.backend.user_service.models;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "Users")
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
    private String bio;
    private String profilePic; // image url from cloud buckets
    private Preferences preferences;
    private LocalDateTime createdAt;

    @DocumentReference
    private List<String> ownedGames;

    public User(String username, String firstName, String lastName, String email, String password, String bio,
            Preferences preferences, List<String> ownedgames) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.bio = bio;
        this.preferences = preferences;
        this.ownedGames = ownedgames;
        this.createdAt = LocalDateTime.now();
    }

}
