package com.boardwise.backend.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.boardwise.backend.user_service.models.Notification;

public interface NotificationRepository extends MongoRepository<Notification, String>{

}
