package com.boardwise.backend.user_service.dtos.notifications;

public sealed interface ChatNotification extends NotificationDTO 
permits CommunityMessageNotification, DirectMessageNotification{
    String senderId();
    String message();
}
