package com.boardwise.backend.user_service.dtos;

public record CommunityChatNotification(
    String type,
    String senderId,
    String message

) implements Notification{

    public CommunityChatNotification(String senderId, String message){
        this("COMMUNITY_CHAT", senderId, message);
    }

    @Override
    public String getType(){
        return type;
    }

}   
