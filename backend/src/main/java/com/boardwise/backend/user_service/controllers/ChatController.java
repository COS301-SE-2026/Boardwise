package com.boardwise.backend.user_service.controllers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.boardwise.backend.user_service.dtos.CommunityMessage;
import com.boardwise.backend.user_service.dtos.CommunityMessageDTO;
import com.boardwise.backend.user_service.dtos.DirectMessage;
import com.boardwise.backend.user_service.dtos.DirectMessageDTO;
import com.boardwise.backend.user_service.dtos.ErrorMessage;
import com.boardwise.backend.user_service.dtos.MessagesDTO;
import com.boardwise.backend.user_service.enums.MessageType;
import com.boardwise.backend.user_service.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;



@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService service;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/direct")
    public void processDirectMessage(
        @Payload DirectMessage message,
        Principal principal
    ){
        DirectMessageDTO chatMessage = service.handleDirectMessage(principal, message);
        messagingTemplate.convertAndSendToUser(
            message.receiverId(),
            "/queue/chat",
            chatMessage
        );
    }

    @MessageMapping("/chat/community")
    public void processCommunityMessage(
        @Payload CommunityMessage message,
        Principal principal
    ){  
        CommunityMessageDTO chatMessage = service.handleCommunityMessage(principal, message);
        messagingTemplate.convertAndSend(
            "/topic/community/" + message.communityId() + "/chat",
            chatMessage
        );
    }

    @GetMapping("/")
    public ResponseEntity<?> getMessages(
        @RequestHeader("Authorization") String bearer,
        @RequestParam MessageType type, // DIRECT | COMMUNITY
        @RequestParam(required = false) String cId, // set ONLY if type is community
        @RequestParam(required = false) Integer page
    ){
        try{
            String token = bearer.substring(7);
            MessagesDTO res = service.retrieveMessages(token, type, cId, page);
            return ResponseEntity.ok().body(res);
        }
        catch(IllegalArgumentException e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(res);
        }
        catch(Exception e){
            Map<String, Object> res = new HashMap<>();
            res.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    @MessageExceptionHandler(NoSuchElementException.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleNoSuchElementException(NoSuchElementException e){
        String type = e.getMessage().contains("User") ? "USER_NOT_FOUND" : "COMMUNITY_NOT_FOUND";
        return new ErrorMessage(type, e.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ErrorMessage handleGenericException(Exception e){
        return new ErrorMessage("INTERNAL_ERROR", "Something went wrong on our end.");
    }
}
