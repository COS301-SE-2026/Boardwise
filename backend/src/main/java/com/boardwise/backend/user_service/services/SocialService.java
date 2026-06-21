package com.boardwise.backend.user_service.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
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

@Service
public class SocialService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupMembershipRepository gmRepo;
    private final JWTService jwtService;

    SocialService(UserRepository userRepo, GroupRepository groupRepo, GroupMembershipRepository gmRepo, JWTService jwtService) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.gmRepo = gmRepo;
        this.jwtService = jwtService;
    }

    public GroupCreationResponseDTO createGroup(String token, GroupCreationDTO group) {
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();

        String groupName = AuthService.sanitize(group.name());
        String groupDesc = AuthService.sanitize(group.description());
        String groupCategory = AuthService.sanitize(group.category());
        String visibility = group.visisbility() == null ? "public" : group.visisbility();

        Group newGroup = new Group(
            groupName, 
            groupDesc, 
            groupCategory,
            user.getId(), 
            visibility
        );
        newGroup = groupRepo.save(newGroup);

        GroupMembership membership = new GroupMembership(
            user.getId(), 
            newGroup.getId()
        );
        gmRepo.save(membership);

        GroupInfo info = new GroupInfo(
            newGroup.getId(),
            newGroup.getName(),
            newGroup.getDescription(),
            user.getUsername(),
            newGroup.getVisibility(),
            1
        );

        return new GroupCreationResponseDTO(
            "Group created successfully",
            info
        );
    }

    public List<?> getAllGroups(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();
        List<GroupInfo> groups = new ArrayList<>();

        for(Group group : groupRepo.findAll()){
            if(group.getVisibility().equalsIgnoreCase("private")){
                GroupMembership toCheck = new GroupMembership();
                toCheck.setGroupId(group.getId());
                toCheck.setUserId(user.getId());

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
                group.getDescription(),
                owner.getUsername(),
                group.getVisibility(),
                memberCount
            );

            groups.add(info);
        }

        return groups;
    }

    public GroupDTO getGroup(String token, String groupId) {
        String currentUsername = jwtService.extractUsername(token);
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

            if(member.getUsername().equals(currentUsername))
                isMember = true;

            Map<String, String> userData = new HashMap<>();
            userData.put("username", member.getUsername());
            userData.put("profilePicture", member.getProfilePicture());
            members.add(userData);
        }

        return new GroupDTO(
            group.getId(),
            group.getName(),
            group.getDescription(),
            owner.getUsername(),
            memberCount,
            members,
            isMember
        );

    }

    public GroupMembershipResponseDTO addToGroup(String token, String groupId) {
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();
        Group group = groupRepo.findById(groupId).orElseThrow();

        GroupMembership gm = new GroupMembership();
        gm.setGroupId(group.getId());
        gm.setUserId(user.getId());

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
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();
        Group group = groupRepo.findById(groupId).orElseThrow();
        
        GroupMembership example = new GroupMembership();
        example.setGroupId(group.getId());
        example.setUserId(user.getId());

        if(!gmRepo.exists(Example.of(example)))
            throw new IllegalStateException("User is not a member of this group.");

        gmRepo.deleteByUserIdAndGroupId(user.getId(), group.getId());

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

    public GroupInfo getGroup(String groupName) {
        String cleanName = AuthService.sanitize(groupName);
        List<Group> groups = groupRepo.findByName(cleanName);
        if(groups.size() < 1)
            throw new NoSuchElementException("Group with that name does not exist");

        Group group = groups.getFirst();
        // get owner
        User owner = userRepo.findById(group.getOwnerId()).get();

        // get memberCount
        GroupMembership gm = new GroupMembership();
        gm.setGroupId(group.getId());
        int memberCount = (int) gmRepo.count(Example.of(gm));

        return new GroupInfo(
            group.getId(),
            group.getName(),
            group.getDescription(),
            owner.getUsername(),
            group.getVisibility(),
            memberCount
        );
    }

    public GroupUpdateResponseDTO updateGroup(String token, String groupId, GroupUpdateRequestDTO updateData) {
        String username = jwtService.extractUsername(token);
        User user = userRepo.findByUsername(username).get();
        Group group = groupRepo.findById(groupId).orElseThrow();
        
        if(!user.getId().equals(group.getOwnerId()))
            throw new IllegalStateException("This user is not the owner of this group");

        String newName = AuthService.sanitize(updateData.name());
        String newDesc = AuthService.sanitize(updateData.description());

        if(newName != null){
            group.setName(newName);
        }
        if(newDesc != null){
            group.setDescription(newDesc);
        }
        Group updatedGroup = groupRepo.save(group);
        Map<String, Object> data = new HashMap<>();
        data.put("name", updatedGroup.getName());
        data.put("description", updatedGroup.getDescription());

        return new GroupUpdateResponseDTO(
            "Successfully updated group information",
            data
        );
    }

}
