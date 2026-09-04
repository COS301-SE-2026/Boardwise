package com.boardwise.backend.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.models.GroupMembership;
import java.util.List;



public interface GroupMembershipRepository extends MongoRepository<GroupMembership, String>{
    List<GroupMembership> findByGroupId(String groupId);
    void deleteByUserIdAndGroupId(String userId, String groupId);
}
