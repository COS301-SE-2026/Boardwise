package com.boardwise.backend.user_service.services;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.GroupDTO;
import com.boardwise.backend.user_service.dtos.GroupInfo;
import com.boardwise.backend.user_service.dtos.notifications.CommunityMessageNotification;
import com.boardwise.backend.user_service.dtos.request.GroupCreationDTO;
import com.boardwise.backend.user_service.dtos.request.GroupInviteRequest;
import com.boardwise.backend.user_service.dtos.request.GroupRemovalRequest;
import com.boardwise.backend.user_service.dtos.request.GroupUpdateRequestDTO;
import com.boardwise.backend.user_service.dtos.response.GroupCreationResponseDTO;
import com.boardwise.backend.user_service.dtos.response.GroupInviteResponse;
import com.boardwise.backend.user_service.dtos.response.GroupMembershipResponseDTO;
import com.boardwise.backend.user_service.dtos.response.GroupUpdateResponseDTO;
import com.boardwise.backend.user_service.enums.GroupMembershipStatus;
import com.boardwise.backend.user_service.enums.ResponseStatus;
import com.boardwise.backend.user_service.enums.Visibility;
import com.boardwise.backend.user_service.events.JoinedCommunityEvent;
import com.boardwise.backend.user_service.events.payload.JoinedCommunityEventPayload;
import com.boardwise.backend.user_service.models.Group;
import com.boardwise.backend.user_service.models.GroupMembership;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.GroupMembershipRepository;
import com.boardwise.backend.user_service.repository.GroupRepository;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupMembershipRepository gmRepo;
    private final JWTService jwtService;
    private final R2StorageService bucket;
    private final ApplicationEventPublisher eventPublisher;
    private final MongoTemplate template;
    private final ObjectMapper objectMapper;


    public GroupCreationResponseDTO createGroup(String token, GroupCreationDTO group, MultipartFile image) throws IOException{
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findById(userId).get();

        String groupName = AuthService.sanitize(group.name());
        String groupDesc = AuthService.sanitize(group.description());
        String groupCategory = AuthService.sanitize(group.category());
        Visibility visibility = group.visibility();
        

        Group newGroup = new Group(
            groupName, 
            null,
            groupDesc, 
            groupCategory,
            userId, 
            visibility
        );
        newGroup = groupRepo.save(newGroup);

        String imageUrl = null;
        if(image != null){
            String fileName = bucket.uploadFile(image, newGroup.getId());
            imageUrl = bucket.getFileUrl(fileName);
            newGroup.setImageUrl(imageUrl);
            newGroup = groupRepo.save(newGroup);
        }

        GroupMembership membership = new GroupMembership(
            userId, 
            newGroup.getId()
        );
        gmRepo.save(membership);

        GroupInfo info = new GroupInfo(
            newGroup.getId(),
            newGroup.getName(),
            newGroup.getImageUrl(),
            newGroup.getDescription(),
            user.getUsername(),
            newGroup.getVisibility(),
            newGroup.getCategory(),
            1,
            GroupMembershipStatus.MEMBER
        );

        return new GroupCreationResponseDTO(
            "Group created successfully",
            info
        );
    }

    public List<?> getAllGroups(String token) {
        String userId = jwtService.extractUserId(token).toString();
        List<GroupInfo> groups = new ArrayList<>();

        for(Group group : groupRepo.findAll()){
            User owner = userRepo.findById(group.getOwnerId()).get();
            
            int memberCount = (int) gmRepo.countByGroupIdAndStatus(group.getId(), GroupMembershipStatus.MEMBER);

            Optional<GroupMembership> membership = gmRepo.findByUserIdAndGroupId(userId, group.getId());

            GroupInfo info = new GroupInfo(
                group.getId(),
                group.getName(),
                group.getImageUrl(),
                group.getDescription(),
                owner.getUsername(),
                group.getVisibility(),
                group.getCategory(),
                memberCount,
                membership.isPresent() ? membership.get().getStatus() : null
            );
            groups.add(info);
        }

        return groups;
    }

    public GroupDTO getGroup(String token, String groupId) throws IllegalAccessException {
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow(
            () -> {
                throw new NoSuchElementException("Group with associated id does not exist");
            }
        );

        Optional<GroupMembership> gm = gmRepo.findByUserIdAndGroupId(userId, group.getId());
        if(
            group.getVisibility() == Visibility.PRIVATE && 
            (gm.isEmpty() || (gm.isPresent() && gm.get().getStatus() != GroupMembershipStatus.MEMBER))
        )
            throw new IllegalAccessException("This user is not a member of this group");

        // get owner
        User owner = userRepo.findById(group.getOwnerId()).get();
        boolean isOwner = owner.getId().equals(userId);

        // get memberCount
        List<GroupMembership> memberships = gmRepo.findAllByGroupIdAndStatus(groupId, GroupMembershipStatus.MEMBER);
        int memberCount = memberships.size();

        // get explicit members
        List<Map<String, String>> members = new ArrayList<>();
        boolean isMember = false;
        for(GroupMembership membership : memberships){
            User member = userRepo.findById(membership.getUserId()).get();
            if(member == null)
                continue;

            if(member.getId().equals(userId))
                isMember = true;

            Map<String, String> userData = new HashMap<>();
            userData.put("id", member.getId());
            userData.put("username", member.getUsername());
            userData.put("profilePicture", member.getProfilePicture());
            members.add(userData);
        }

        return new GroupDTO(
            group.getId(),
            group.getName(),
            group.getImageUrl(),
            group.getDescription(),
            owner.getUsername(),
            group.getVisibility(),
            memberCount,
            members,
            isMember,
            isOwner
        );

    }

    public List<GroupInfo> searchForGroup(String token, String groupName) {
        String userId = jwtService.extractUserId(token).toString();
        String cleanName = AuthService.sanitize(groupName);
        
        Criteria searchCriteria = Criteria.where("name").regex(cleanName, "i");
        Pageable page = PageRequest.of(0, 10);
        Query query = new Query(searchCriteria);
        query.with(page);
        List<Group> matches = template.find(query, Group.class);

        List<GroupInfo> groups = new ArrayList<>();
        for(Group group : matches){
            User owner = userRepo.findById(group.getOwnerId()).get();

            int memberCount = (int) gmRepo.countByGroupIdAndStatus(group.getId(), GroupMembershipStatus.MEMBER);

            Optional<GroupMembership> membership = gmRepo.findByUserIdAndGroupId(userId, group.getId());

            groups.add(new GroupInfo(
                    group.getId(),
                    group.getName(),
                    group.getImageUrl(),
                    group.getDescription(),
                    owner.getUsername(),
                    group.getVisibility(),
                    group.getCategory(),
                    memberCount,
                    membership.isPresent() ? membership.get().getStatus() : null
                )
            );
        }
        return groups;
    }

    public GroupUpdateResponseDTO updateGroup(String token, String groupId, GroupUpdateRequestDTO updateData, MultipartFile image) throws IOException {
        
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow();
        
        if(!userId.equals(group.getOwnerId()))
            throw new IllegalStateException("This user is not the owner of this group");

        String newName = AuthService.sanitize(updateData.name());
        String newDesc = AuthService.sanitize(updateData.description());

        if(newName != null && !group.getName().equals(newName)){
            group.setName(newName);
        }
        if(newDesc != null && !group.getDescription().equals(newDesc)){
            group.setDescription(newDesc);
        }
        if(updateData.visibility() != null && !group.getVisibility().equals(updateData.visibility())){
            group.setVisibility(updateData.visibility());
        }
        if(image != null){
            bucket.deleteFile(group.getImageUrl());
            String fileName = bucket.uploadFile(image, group.getId());
            String imageUrl = bucket.getFileUrl(fileName);
            group.setImageUrl(imageUrl);
            group = groupRepo.save(group);
        }

        Group updatedGroup = groupRepo.save(group);
        Map<String, Object> data = new HashMap<>();
        data.put("name", updatedGroup.getName());
        data.put("description", updatedGroup.getDescription());
        data.put("visibility", updatedGroup.getVisibility());
        data.put("imageUrl", updatedGroup.getImageUrl());

        return new GroupUpdateResponseDTO(
            "Successfully updated group information",
            data
        );
    }

    public GroupMembershipResponseDTO joinGroup(String token, String groupId) throws IllegalAccessException, IllegalStateException {
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow();

        if(group.getVisibility() == Visibility.PRIVATE)
            throw new IllegalAccessException("User must receive invite in order to join private communities");

        GroupMembership gm = new GroupMembership();
        gm.setGroupId(group.getId());
        gm.setUserId(userId);
        gm.setStatus(GroupMembershipStatus.MEMBER);

        if(gmRepo.exists(Example.of(gm)))
            throw new IllegalStateException("User already a member of this group.");

        gm.setJoinedAt(Instant.now());
        gmRepo.save(gm);

        
        Map<String, Object> data = new HashMap<>();

        // new member count
        GroupMembership example = new GroupMembership();
        example.setGroupId(group.getId());
        int memberCount = (int) gmRepo.count(Example.of(example));
        data.put("memberCount", memberCount);

        // new status
        boolean isMember = true;
        data.put("isMember", isMember);

        // new members array
        List<Map<String, String>> members = new ArrayList<>();
        for(GroupMembership membership : gmRepo.findByGroupId(group.getId())){
            User member = userRepo.findById(membership.getUserId()).get();
            if(member == null)
                continue;

            Map<String, String> userData = new HashMap<>();
            userData.put("id", member.getId());
            userData.put("username", member.getUsername());
            userData.put("profilePicture", member.getProfilePicture());
            members.add(userData);
        }
        data.put("members", members);
        
        try{
            User member = userRepo.findById(userId).get();
            Map<String, String> message = new HashMap<>();

            message.put("id", userId);
            message.put("username", member.getUsername());
            message.put("profilePicture", member.getProfilePicture());

            String messageJson = objectMapper.writeValueAsString(message);
            CommunityMessageNotification notification = new CommunityMessageNotification("SYSTEM", messageJson);
            JoinedCommunityEventPayload payload = new JoinedCommunityEventPayload(groupId, notification);
            JoinedCommunityEvent event = new JoinedCommunityEvent(this, payload);
            eventPublisher.publishEvent(event);
        } 
        catch(JsonProcessingException e){
            System.out.println("[Social Service]: Failed to process object into json:\n" + e);
        }

        return new GroupMembershipResponseDTO(
            "Joined group successfully",
            data
        );
        
    }

    public GroupMembershipResponseDTO leaveGroup(String token, String groupId) {
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow();
        
        GroupMembership example = new GroupMembership();
        example.setGroupId(group.getId());
        example.setUserId(userId);
        example.setStatus(GroupMembershipStatus.MEMBER);

        if(!gmRepo.exists(Example.of(example)))
            throw new IllegalStateException("User is not a member of this group.");

        gmRepo.deleteByUserIdAndGroupId(userId, group.getId());

        Map<String, Object> data = new HashMap<>();

        // new member count
        GroupMembership ex = new GroupMembership();
        ex.setGroupId(group.getId());
        int memberCount = (int) gmRepo.count(Example.of(ex));
        data.put("memberCount", memberCount);

        // new status
        boolean isMember = false;
        data.put("isMember", isMember);

        // new members array
        List<Map<String, String>> members = new ArrayList<>();
        for(GroupMembership membership : gmRepo.findByGroupId(group.getId())){
            User member = userRepo.findById(membership.getUserId()).get();
            if(member == null)
                continue;

            Map<String, String> userData = new HashMap<>();
            userData.put("username", member.getUsername());
            userData.put("profilePicture", member.getProfilePicture());
            members.add(userData);
        }
        data.put("members", members);

        try{
            String messageJson = objectMapper.writeValueAsString(data);
            CommunityMessageNotification notification = new CommunityMessageNotification("SYSTEM", messageJson);
            JoinedCommunityEventPayload payload = new JoinedCommunityEventPayload(groupId, notification);
            JoinedCommunityEvent event = new JoinedCommunityEvent(this, payload);
            eventPublisher.publishEvent(event);
        } 
        catch(JsonProcessingException e){
            System.out.println("[Social Service]: Failed to process object into json:\n" + e);
        }
        
        return new GroupMembershipResponseDTO(
            "Group exited successfully",
            data
        );

    }

    // send invite
    public Map<String, String> inviteToGroup(String token, String groupId, GroupInviteRequest invite) throws IllegalAccessException, NoSuchElementException{
        Map<String, String> result = new HashMap<>();
        String ownerId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow(
            () -> new NoSuchElementException("Group with associated id does not exist")
        );

        if(!group.getOwnerId().equals(ownerId))
            throw new IllegalAccessException("This user is not the owner of this group. Only the owner may send group invites");

        GroupMembership membership = new GroupMembership();
        membership.setGroupId(groupId);
        membership.setUserId(invite.userId());
        membership.setStatus(GroupMembershipStatus.INVITED);
        gmRepo.save(membership);

        // send notification to the receiver


        result.put("message", "Group invite sent successfully.");
        return result;
    }

    // respond to invite
    public Map<String, String> respondToGroupInvite(String token, String groupId, GroupInviteResponse invite) throws IllegalAccessException, IllegalArgumentException, NoSuchElementException{
        Map<String, String> result = new HashMap<>();
        String userId = jwtService.extractUserId(token).toString();

        if(!groupRepo.existsById(groupId))
            throw new NoSuchElementException("Group with associated id does not exist.");

        Optional<GroupMembership> optGm = gmRepo.findByUserIdAndGroupId(userId, groupId);
        if(optGm.isEmpty() || (optGm.isPresent() && optGm.get().getStatus() != GroupMembershipStatus.INVITED))
            throw new IllegalAccessException("No invite to this group was sent to this user.");

        GroupMembership gm = optGm.get();
        if(invite.status() == ResponseStatus.ACCEPT){
            gm.setStatus(GroupMembershipStatus.MEMBER);
            gm.setJoinedAt(Instant.now());
        }
        else if(invite.status() == ResponseStatus.DECLINE){
            gmRepo.delete(gm);
        }
        else{
            throw new IllegalArgumentException("Invite response must be \"ACCEPT\" or \"DECLINE\"");
        }

        result.put("message", "Group invite response successfully recorded.");
        return result;
    }

    // Kick User from community
    public Map<String, String> kickMemberFromGroup(String token, String groupId, GroupRemovalRequest data) throws IllegalAccessException, NoSuchElementException{
        Map<String, String> result = new HashMap<>();
        String ownerId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow(
            () -> new NoSuchElementException("Group with associated id does not exist")
        );

        if(!group.getOwnerId().equals(ownerId))
            throw new IllegalAccessException("This user is not the owner of this group. Only the owner may remove users from a group");

        gmRepo.deleteByUserIdAndGroupId(data.memberId(), groupId);

        result.put("message", "User successfully removed from the group");
        return result;
    }

    public Object getGroupInvites(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getGroupInvites'");
    }

}
