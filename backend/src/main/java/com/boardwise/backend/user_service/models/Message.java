package com.boardwise.backend.user_service.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boardwise.backend.user_service.enums.MessageType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "MESSAGE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    private String id;
    private MessageType type;
    private String senderId;
    private String receiverId; // can be a user or community depending on the type ('Direct' | 'Community') of the message.
    private String message;
    private Instant sentAt;

}
