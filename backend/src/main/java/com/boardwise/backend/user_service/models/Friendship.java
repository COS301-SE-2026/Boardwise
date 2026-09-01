package com.boardwise.backend.user_service.models;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boardwise.backend.user_service.enums.FriendStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "FRIENDSHIP")
@CompoundIndex(def = "{'sender': 1, 'receiver': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {

    @Id
    private String id;

    private String sender;
    private String receiver;
    private FriendStatus status;
    private Instant createdAt;

    public Friendship(String sender, String receiver){
        this.sender = sender;
        this.receiver = receiver;
        this.status = FriendStatus.REQUESTED;
        this.createdAt = Instant.now();
    }
}   
