package com.boardwise.backend.user_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.boardwise.backend.user_service.enums.GroupMembershipStatus;
import com.boardwise.backend.user_service.models.GroupMembership;
import java.util.List;
import java.util.Optional;



public interface GroupMembershipRepository extends MongoRepository<GroupMembership, String>{
    List<GroupMembership> findByGroupId(String groupId);

    void deleteByUserIdAndGroupId(String userId, String groupId);

    Optional<GroupMembership> findByUserIdAndGroupId(String userId, String groupId);

    List<GroupMembership> findAllByGroupIdAndStatus(String groupId, GroupMembershipStatus status);

    List<GroupMembership> findAllByUserIdAndStatus(String userId, GroupMembershipStatus status);

    long countByGroupIdAndStatus(String groupId, GroupMembershipStatus status);
}
