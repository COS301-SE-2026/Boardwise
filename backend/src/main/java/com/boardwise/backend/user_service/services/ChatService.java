package com.boardwise.backend.user_service.services;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.CommunityMessage;
import com.boardwise.backend.user_service.dtos.CommunityMessageDTO;
import com.boardwise.backend.user_service.dtos.CommunityMessageNotification;
import com.boardwise.backend.user_service.dtos.DirectMessage;
import com.boardwise.backend.user_service.dtos.DirectMessageDTO;
import com.boardwise.backend.user_service.dtos.DirectMessageNotification;
import com.boardwise.backend.user_service.dtos.MessagesDTO;
import com.boardwise.backend.user_service.dtos.NotificationDTO;
import com.boardwise.backend.user_service.enums.MessageType;
import com.boardwise.backend.user_service.models.Message;
import com.boardwise.backend.user_service.repository.GroupRepository;
import com.boardwise.backend.user_service.repository.MessageRepository;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final NotificationService notifService;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final JWTService jwtService;
    private final MongoTemplate db;
    
    
    public DirectMessageDTO handleDirectMessage(Principal principal, DirectMessage message){
        if(!userRepo.existsById(message.receiverId())) 
            throw new NoSuchElementException("User associated with id: " + message.receiverId() + " does not exist.");
        
        // send the notification
        String senderId = principal.getName();
        
        // send the chat message for the ui
        // save the message first
        Message toStore = new Message(
            message.id(),
            MessageType.DIRECT,
            senderId,
            message.receiverId(),
            null,
            message.message(),
            false,
            Instant.now()
        );

        try{
            messageRepo.save(toStore);
        }
        catch(DuplicateKeyException e){
            return new DirectMessageDTO(
                senderId, 
                message
            );
        }

        NotificationDTO notification = new DirectMessageNotification(
            senderId,
            message.message()
        );
        notifService.notifyUser(message.receiverId(), notification);
        
        return new DirectMessageDTO(
            senderId,
            message
        );
    }

    public CommunityMessageDTO handleCommunityMessage(Principal principal, CommunityMessage message){
        if(!groupRepo.existsById(message.communityId())) 
            throw new NoSuchElementException("Community associated with id: " + message.communityId() + " does not exist.");
        
        String senderId = principal.getName();

        // send the chat message for the ui (senderId will be used)
        // save the message first 
        Message toStore = new Message(
            message.id(),
            MessageType.COMMUNITY,
            senderId,
            null,
            message.communityId(),
            message.message(),
            false,
            Instant.now()
        );
        try{
            messageRepo.save(toStore);
        }
        catch(DuplicateKeyException e){
            return new CommunityMessageDTO(
                senderId, 
                message
            );
        }

        NotificationDTO notification = new CommunityMessageNotification(
            senderId, 
            message.message()
        );
        notifService.notifyCommunity(message.communityId(), notification);

        return new CommunityMessageDTO(
            senderId, 
            message
        );
    }

    public MessagesDTO retrieveMessages(String token, MessageType type, String cId, Integer page) {
        String userId = jwtService.extractUserId(token).toString();
        Instant lastOnline = userRepo.findById(userId)
                                    .map(user -> user.getLastOnlineAt())
                                    .orElse(Instant.EPOCH);
        List<?> messages;
        Query query;
        Criteria criteria;
        int pageSize = 75;
        Pageable pageable = PageRequest.of(
            page == null ? 0 : page,
            pageSize,
            Sort.by(Sort.Direction.ASC, "sentAt")
        );

        if(type == MessageType.DIRECT){
            criteria = Criteria.where("recipientUserId").is(userId).and("sentAt").gt(lastOnline);
            query = Query.query(criteria).with(pageable);
            messages = db.find(query, Message.class).stream()
                            .map((msg) -> new DirectMessageDTO(
                                msg.getId(),
                                msg.getSenderId(),
                                msg.getRecipientUserId(),
                                msg.getMessage()
                            )).toList();
        }
        else if(type == MessageType.COMMUNITY){
            if(cId == null)
                throw new IllegalArgumentException("'cId' field is required for requests of type: COMMUNITY");

            criteria = Criteria.where("communityId").is(cId).and("sentAt").gt(lastOnline);
            query = Query.query(criteria).with(pageable);
            messages = db.find(query, Message.class).stream()
                            .map((msg) -> new CommunityMessageDTO(
                                msg.getId(),
                                msg.getSenderId(),
                                msg.getCommunityId(),
                                msg.getMessage()
                            )).toList();
            
        }
        else throw new IllegalArgumentException("Type query parameter must be DIRECT or COMMUNITY");
        
        return new MessagesDTO(
            "User messages retrieved successfully",
            messages
        );
    }
}
