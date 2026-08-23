package com.boardwise.backend.user_service.dtos;

public record FriendRequestNotification(
    String type,
    FriendRequestDTO request
) implements NotificationDTO {
    public FriendRequestNotification(FriendRequestDTO request){
        this("FRIEND_REQUEST", request);
    }

    @Override
    public String getType() {
        return type;
    }   
}
