package com.boardwise.backend.user_service.dtos;

public record FriendDTO(
    String id,
    String username,
    String fullname,
    String profilePicture
) {

    @Override
    public boolean equals(Object o){
        if(o instanceof FriendDTO){
            FriendDTO other = (FriendDTO) o;
            return this.id.equals(other.id);
        }
        return false;
    }
}
