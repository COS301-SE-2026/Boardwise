package com.boardwise.backend.user_service.fixtures;

import com.boardwise.backend.user_service.enums.FriendStatus;
import com.boardwise.backend.user_service.models.Friendship;
import com.boardwise.backend.user_service.models.User;

public class ProfileServiceFixtures {

    public static final String OWNER_ID = "507f1f77bcf86cd799439011";
    public static final String FRIEND_ID1 = "507f1f77bcf86cd799439012";
    public static final String FRIEND_ID2 = "507f1f77bcf86cd799439013";
    public static final String FRIEND_ID3 = "507f1f77bcf86cd799439014";

    // Users 
    public static User owner(){
        User user = new User(
            "owner_user",
            "Alice",
            "Smith",
            "alice@gmail.com",
            "@lice's strong passw0rd"
        );

        user.setId(OWNER_ID);
        return user;
    }

    public static User friend1(){
        User user = new User(
            "friend_one",
            "Bob",
            "Jones",
            "bob@gmail.com",
            "Bob's strong passw0rd"
        );

        user.setId(FRIEND_ID1);
        return user;
    }

    public static User friend2(){
        User user = new User(
            "friend_two",
            "Carol",
            "White",
            "carol@gmail.com",
            "C@rol's strong passw0rd"
        );

        user.setId(FRIEND_ID2);
        return user;
    }

    public static User friend3(){
        User user = new User(
            "friend_three",
            "Dan",
            "Brown",
            "dan@gmail.com",
            "D@n's strong passw0rd"
        );

        user.setId(FRIEND_ID3);
        return user;
    }

    // Friendships (Friend1 is a mutual of friend3 and Owner)
    public static Friendship friendship1(){
        Friendship fs = new Friendship(OWNER_ID, FRIEND_ID1);
        fs.setStatus(FriendStatus.ACCEPTED);
        fs.setId("fs-001");
        return fs;
    }

    public static Friendship friendship2(){
        Friendship fs = new Friendship(FRIEND_ID2, OWNER_ID);
        fs.setStatus(FriendStatus.ACCEPTED);
        fs.setId("fs-002");
        return fs;
    }

    public static Friendship friendship3(){
        Friendship fs = new Friendship(FRIEND_ID3, FRIEND_ID1);
        fs.setStatus(FriendStatus.ACCEPTED);
        fs.setId("fs-003");
        return fs;
    }

    // friendships (as friend requests)
    public static Friendship friendship4(){
        Friendship fs = new Friendship(FRIEND_ID1, FRIEND_ID2);
        fs.setId("fs-004");
        return fs;
    }

    public static Friendship friendship5(){
        Friendship fs = new Friendship(FRIEND_ID3, FRIEND_ID2);
        fs.setId("fs-005");
        return fs;
    }

}
