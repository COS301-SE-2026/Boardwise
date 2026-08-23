package com.boardwise.backend.user_service.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boardwise.backend.user_service.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "Notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;
    private NotificationType type;
    private String message;
    private Instant sentAt;
}
