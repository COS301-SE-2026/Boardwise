package com.boardwise.backend.user_service.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boardwise.backend.user_service.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "MESSAGE")
@CompoundIndexes({
    @CompoundIndex(name = "direct_chat_history", def = "{ 'senderId': 1, 'recipientUserId': 1, 'sentAt': -1 }"),
    @CompoundIndex(name = "community_chat_history", def = "{ 'communityId': 1, 'sentAt': -1 }")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String id;
    private MessageType type;
    private String senderId;
    private String recipientUserId; // set when type is DIRECT
    private String communityId; // set when type is COMMUNITY
    private String message;
    private boolean deleted;
    private Instant sentAt;

}
