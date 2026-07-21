package com.boardwise.backend.user_service.services;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Example;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.dtos.GroupCreationDTO;
import com.boardwise.backend.user_service.dtos.GroupCreationResponseDTO;
import com.boardwise.backend.user_service.dtos.GroupDTO;
import com.boardwise.backend.user_service.dtos.GroupInfo;
import com.boardwise.backend.user_service.dtos.GroupMembershipResponseDTO;
import com.boardwise.backend.user_service.dtos.GroupUpdateRequestDTO;
import com.boardwise.backend.user_service.dtos.GroupUpdateResponseDTO;
import com.boardwise.backend.user_service.models.Group;
import com.boardwise.backend.user_service.models.GroupMembership;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repos.GroupMembershipRepository;
import com.boardwise.backend.user_service.repos.GroupRepository;
import com.boardwise.backend.user_service.repos.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupMembershipRepository gmRepo;
    private final JWTService jwtService;
    private final R2StorageService bucket;
    private final MongoTemplate template;


    public GroupCreationResponseDTO createGroup(String token, GroupCreationDTO group, MultipartFile image) throws IOException{
        String userId = jwtService.extractUserId(token).toString();
        User user = userRepo.findByUsername(userId).get();

        String groupName = AuthService.sanitize(group.name());
        String groupDesc = AuthService.sanitize(group.description());
        String groupCategory = AuthService.sanitize(group.category());
        String visibility = group.visisbility() == null ? "public" : group.visisbility();
        

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
            1
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
            if(group.getVisibility().equalsIgnoreCase("private")){
                GroupMembership toCheck = new GroupMembership();
                toCheck.setGroupId(group.getId());
                toCheck.setUserId(userId);

                if(!gmRepo.exists(Example.of(toCheck)))
                    continue;
            }

            User owner = userRepo.findById(group.getOwnerId()).get();
            
            GroupMembership gm = new GroupMembership();
            gm.setGroupId(group.getId());

            int memberCount = (int) gmRepo.count(Example.of(gm));
            GroupInfo info = new GroupInfo(
                group.getId(),
                group.getName(),
                group.getImageUrl(),
                group.getDescription(),
                owner.getUsername(),
                group.getVisibility(),
                group.getCategory(),
                memberCount
            );

            groups.add(info);
        }

        return groups;
    }

    public GroupDTO getGroup(String token, String groupId) {
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow(
            () -> {
                throw new NoSuchElementException("Group with associated id does not exist");
            }
        );
        // get owner
        User owner = userRepo.findById(group.getOwnerId()).get();

        // get memberCount
        GroupMembership gm = new GroupMembership();
        gm.setGroupId(group.getId());
        int memberCount = (int) gmRepo.count(Example.of(gm));

        // get explicit members
        List<Map<String, String>> members = new ArrayList<>();
        boolean isMember = false;
        for(GroupMembership membership : gmRepo.findByGroupId(groupId)){
            User member = userRepo.findById(membership.getUserId()).get();
            if(member == null)
                continue;

            if(member.getId().equals(userId))
                isMember = true;

            Map<String, String> userData = new HashMap<>();
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
            memberCount,
            members,
            isMember
        );

    }

    public GroupMembershipResponseDTO addToGroup(String token, String groupId) {
         // TODO: return the entire resource with update applied
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow();

        GroupMembership gm = new GroupMembership();
        gm.setGroupId(group.getId());
        gm.setUserId(userId);

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
            userData.put("username", member.getUsername());
            userData.put("profilePicture", member.getProfilePicture());
            members.add(userData);
        }
        data.put("members", members);

        return new GroupMembershipResponseDTO(
            "Joined group successfully",
            data
        );
        
    }

    public GroupMembershipResponseDTO removeFromGroup(String token, String groupId) {
         // TODO: return the entire resource with update applied
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow();
        
        GroupMembership example = new GroupMembership();
        example.setGroupId(group.getId());
        example.setUserId(userId);

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
        
        return new GroupMembershipResponseDTO(
            "Group exited successfully",
            data
        );

    }

    public List<GroupInfo> getGroup(String groupName) {
        String cleanName = AuthService.sanitize(groupName);
        Criteria searchCriteria = Criteria.where("name").regex(cleanName, "i");
        Query query = new Query(searchCriteria);
        List<Group> groups = template.find(query, Group.class);

        List<GroupInfo> matches = new ArrayList<>();
        for(Group group : groups){
            User owner = userRepo.findById(group.getOwnerId()).get();

            // get memberCount
            GroupMembership gm = new GroupMembership();
            gm.setGroupId(group.getId());
            int memberCount = (int) gmRepo.count(Example.of(gm));

            matches.add(new GroupInfo(
                    group.getId(),
                    group.getName(),
                    group.getImageUrl(),
                    group.getDescription(),
                    owner.getUsername(),
                    group.getVisibility(),
                    group.getCategory(),
                    memberCount
                )
            );
        }
        return matches;
    }

    public GroupUpdateResponseDTO updateGroup(String token, String groupId, GroupUpdateRequestDTO updateData, MultipartFile image) throws IOException {
        // TODO: return the entire resource with update applied
        
        String userId = jwtService.extractUserId(token).toString();
        Group group = groupRepo.findById(groupId).orElseThrow();
        
        if(!userId.equals(group.getOwnerId()))
            throw new IllegalStateException("This user is not the owner of this group");

        String newName = AuthService.sanitize(updateData.name());
        String newDesc = AuthService.sanitize(updateData.description());

        if(newName != null){
            group.setName(newName);
        }
        if(newDesc != null){
            group.setDescription(newDesc);
        }
        if(image != null){
            String fileName = bucket.uploadFile(image, group.getId());
            String imageUrl = bucket.getFileUrl(fileName);
            group.setImageUrl(imageUrl);
            group = groupRepo.save(group);
        }

        Group updatedGroup = groupRepo.save(group);
        Map<String, Object> data = new HashMap<>();
        data.put("name", updatedGroup.getName());
        data.put("description", updatedGroup.getDescription());
        data.put("imageUrl", updatedGroup.getImageUrl());

        return new GroupUpdateResponseDTO(
            "Successfully updated group information",
            data
        );
    }

}
