package com.boardwise.backend.user_service.models;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "CONVERSATIONS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    @Id
    private String id;
    private List<String> participantIds;
    private String lastMessage;
    private Instant lastMessageAt;
}
