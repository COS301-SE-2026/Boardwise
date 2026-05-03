package com.boardwise.backend.user_service.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
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
    private String username;
    private String emailAddress;
    private String password;
    private String firstName;
    private String lastName;
    private String bio;
    private String profilePic; // image url from cloud buckets
    private Preferences preferences;
    private LocalDateTime createdAt;

    @DocumentReference
    private Boardgame ownedGames;

}
