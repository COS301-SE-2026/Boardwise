package com.boardwise.backend.user_service.dtos;

public sealed interface ChatNotification extends NotificationDTO 
permits CommunityMessageNotification, DirectMessageNotification{
    String senderId();
    String message();
}
