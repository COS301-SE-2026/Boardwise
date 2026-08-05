package com.boardwise.backend.user_service.models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "GROUP_MEMBERSHIP")
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class GroupMembership {

    @Id
    private String id;
    private String userId;
    private String groupId;
    private Instant joinedAt;

    public GroupMembership(String userId, String groupId){
        this.userId = userId;
        this.groupId = groupId;
        this.joinedAt = Instant.now();
    }

}
