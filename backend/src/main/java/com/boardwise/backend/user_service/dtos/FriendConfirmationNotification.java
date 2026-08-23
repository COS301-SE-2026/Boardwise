package com.boardwise.backend.user_service.dtos;

public record FriendConfirmationNotification(
    String type,
    FriendDTO friend
) implements NotificationDTO{

    public FriendConfirmationNotification(FriendDTO friend){
        this("FRIEND_CONFIRMATION", friend);
    }

    @Override
    public String getType(){
        return type;
    }
}
