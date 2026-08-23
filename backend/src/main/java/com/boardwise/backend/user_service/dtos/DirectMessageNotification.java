package com.boardwise.backend.user_service.dtos;

public record DirectMessageNotification(
    String type,
    String senderId,
    String message
) implements NotificationDTO{

    public DirectMessageNotification(String senderId, String message){
        this("DIRECT_MESSAGE", senderId, message);
    }

    @Override
    public String getType(){
        return type;
    }
}
