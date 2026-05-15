package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "Friendship")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Friendship {

    @Id
    private String id;
    @Field("user_a_id")
    private String userAId;
    @Field("user_b_id")
    private String userBId;
    @Field("created_at")
    private Instant createdAt;

    public Friendship(String userAId, String userBId){
        this.userAId = userAId;
        this.userBId = userBId;
        this.createdAt = Instant.now();
    }
}   
