package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "FRIENDSHIP")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Friendship {

    @Id
    private String id;

    private String userAId;
    private String userBId;
    private Instant createdAt;

    public Friendship(String userAId, String userBId){
        this.userAId = userAId;
        this.userBId = userBId;
        this.createdAt = Instant.now();
    }
}   
