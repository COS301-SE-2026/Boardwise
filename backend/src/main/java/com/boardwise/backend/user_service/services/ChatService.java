package com.boardwise.backend.user_service.services;

import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
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
// import com.boardwise.backend.shared.services.NotificationService;
import com.boardwise.backend.user_service.dtos.CommunityMessage;
import com.boardwise.backend.user_service.dtos.CommunityMessageDTO;
import com.boardwise.backend.user_service.dtos.ConversationDTO;
import com.boardwise.backend.user_service.dtos.ConversationsResponseDTO;
// import com.boardwise.backend.user_service.dtos.CommunityMessageNotification;
import com.boardwise.backend.user_service.dtos.DirectMessage;
import com.boardwise.backend.user_service.dtos.DirectMessageDTO;
// import com.boardwise.backend.user_service.dtos.DirectMessageNotification;
import com.boardwise.backend.user_service.dtos.MessagesDTO;
// import com.boardwise.backend.user_service.dtos.NotificationDTO;
import com.boardwise.backend.user_service.enums.MessageType;
import com.boardwise.backend.user_service.models.Conversation;
import com.boardwise.backend.user_service.models.Message;
import com.boardwise.backend.user_service.models.User;
import com.boardwise.backend.user_service.repository.ConversationRepository;
import com.boardwise.backend.user_service.repository.GroupRepository;
import com.boardwise.backend.user_service.repository.MessageRepository;
import com.boardwise.backend.user_service.repository.UserRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    // private final NotificationService notifService;
    private final MessageRepository messageRepo;
    private final ConversationRepository convoRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final JWTService jwtService;
    private final MongoTemplate db;
    
    
    public DirectMessageDTO handleDirectMessage(Principal principal, DirectMessage message){
        if(!userRepo.existsById(message.receiverId())) 
            throw new NoSuchElementException("User associated with id: " + message.receiverId() + " does not exist.");
        
        // send the notification
        String senderId = principal.getName();
        String convoId = generateConversationId(senderId, message.receiverId());
        Instant messageTime = Instant.now();
        // send the chat message for the ui
        // save the message first
        Conversation conversation = new Conversation(
            convoId,
            List.of(senderId, message.receiverId()),
            message.message(),
            messageTime
        );
        convoRepo.save(conversation);

        Message toStore = new Message(
            message.id(),
            MessageType.DIRECT,
            convoId,
            senderId,
            message.message(),
            false,
            messageTime
        );

        try{
            messageRepo.save(toStore);
        }
        catch(DuplicateKeyException e){
            return new DirectMessageDTO(
                senderId, 
                message,
                messageTime
            );
        }

        // NotificationDTO notification = new DirectMessageNotification(
        //     senderId,
        //     message.message()
        // );
        // notifService.notifyUser(message.receiverId(), notification);
        
        return new DirectMessageDTO(
            senderId,
            message,
            messageTime
        );
    }

    public CommunityMessageDTO handleCommunityMessage(Principal principal, CommunityMessage message){
        if(!groupRepo.existsById(message.communityId())) 
            throw new NoSuchElementException("Community associated with id: " + message.communityId() + " does not exist.");
        
        String senderId = principal.getName();
        Instant sentAt = Instant.now();

        // send the chat message for the ui (senderId will be used)
        // save the message first 
        Message toStore = new Message(
            message.id(),
            MessageType.COMMUNITY,
            message.communityId(),
            senderId,
            message.message(),
            false,
            sentAt
        );

        try{
            messageRepo.save(toStore);
        }
        catch(DuplicateKeyException e){
            return new CommunityMessageDTO(
                senderId, 
                message,
                sentAt
            );
        }

        // NotificationDTO notification = new CommunityMessageNotification(
        //     senderId, 
        //     message.message()
        // );
        // notifService.notifyCommunity(message.communityId(), notification);

        return new CommunityMessageDTO(
            senderId, 
            message,
            sentAt
        );
    }

    public MessagesDTO retrieveMessages(String token, MessageType type, String targetId, Integer page, Instant since) {
        String userId = jwtService.extractUserId(token).toString();
        Instant lastOnline = (since != null) ? since : Instant.EPOCH;
        
        List<?> messages;
        Query query;
        Criteria criteria;
        int pageSize = 50;
        Pageable pageable = PageRequest.of(
            page == null ? 0 : (page - 1 < 0 ? 0 : page - 1),
            pageSize,
            Sort.by(Sort.Direction.ASC, "sentAt")
        );

        if(type == MessageType.DIRECT){
            criteria = Criteria.where("targetId").is(targetId).and("sentAt").gt(lastOnline);
            query = Query.query(criteria).with(pageable);


            messages = db.find(query, Message.class).stream()
                            .map((msg) -> {
                                String[] userIds = targetId.split("_");
                                String recipient = userIds[0].equals(userId) ? userIds[1] : userIds[0];
                                return new DirectMessageDTO(
                                    msg.getId(),
                                    msg.getSenderId(),
                                    recipient,
                                    msg.getMessage(),
                                    msg.getSentAt()
                                );
                            }).toList();
        }
        else if(type == MessageType.COMMUNITY){
            criteria = Criteria.where("targetId").is(targetId).and("sentAt").gt(lastOnline);
            query = Query.query(criteria).with(pageable);
            messages = db.find(query, Message.class).stream()
                            .map((msg) -> new CommunityMessageDTO(
                                msg.getId(),
                                msg.getSenderId(),
                                targetId,
                                msg.getMessage(),
                                msg.getSentAt()
                            )).toList();
            
        }
        else throw new IllegalArgumentException("Type query parameter must be DIRECT or COMMUNITY");
        
        return new MessagesDTO(
            "User messages retrieved successfully",
            messages
        );
    }

    public ConversationsResponseDTO retrieveConversations(String token) {
        String clientId = jwtService.extractUserId(token).toString();
        List<Conversation> dbResults = convoRepo.participantIdsContainsUserId(clientId);
        List<ConversationDTO> conversations = new ArrayList<>();

        for(Conversation convo : dbResults){
            try{
                int idx = convo.getParticipantIds().indexOf(clientId) == 0 ? 1 : 0;
                String userId = convo.getParticipantIds().get(idx);
                User user = userRepo.findById(userId).orElseThrow(
                    () -> new NoSuchElementException(userId)
                );
                ConversationDTO dto = new ConversationDTO(
                    convo.getId(),
                    userId,
                    user.getUsername(),
                    user.getProfilePicture(),
                    convo.getLastMessage(),
                    convo.getLastMessageAt()
                );
                conversations.add(dto);
            }
            catch(NoSuchElementException e){
                ConversationDTO dto = new ConversationDTO(
                    convo.getId(),
                    e.getMessage(),
                    "Boardwise user",
                    null,
                    convo.getLastMessage(),
                    convo.getLastMessageAt()
                );
                conversations.add(dto);
            }
        }

        return new ConversationsResponseDTO(
            "User conversations successfully retrieved",
            conversations
        );
    }

    public static String generateConversationId(String userA, String userB){
        if(userA == null || userB == null)
            throw new IllegalArgumentException("Both User IDs cannot be null");

        if(userA.compareTo(userB) < 0)
            return userA + "_" + userB;
        
        return userB + "_" + userA;
    }
}
