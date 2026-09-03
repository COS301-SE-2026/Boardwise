package com.boardwise.backend.user_service.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Unwrapped.Nullable;

import com.boardwise.backend.user_service.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "NOTIFICATIONS")
@CompoundIndex(def = "{'userId': 1, 'sentAt': -1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    private String id;
    private String userId; // the person the notification is meant for
    private NotificationType type;
    private NotificationData data; 
    private Instant sentAt;
    private boolean delivered;

    @Nullable
    @Indexed(expireAfter = "7d")
    private Instant deliveredAt;
}
