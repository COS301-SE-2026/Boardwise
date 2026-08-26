package com.boardwise.backend.user_service.dtos;

public record FriendConfirmationNotification(
    String type,
    FriendDTO friend
) implements Notification{

    public FriendConfirmationNotification(FriendDTO friend){
        this("FRIEND_CONFIRMATION", friend);
    }

    @Override
    public String getType(){
        return type;
    }
}
