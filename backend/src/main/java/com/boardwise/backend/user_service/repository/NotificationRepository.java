package com.boardwise.backend.user_service.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.boardwise.backend.user_service.models.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String>{

    @Query("{ 'userId': ?0, 'delivered': false, 'sentAt': { '$gt': ?2 } }")
    public List<Notification> findMissedNotifications(String userId, Instant lastOnline);

}
