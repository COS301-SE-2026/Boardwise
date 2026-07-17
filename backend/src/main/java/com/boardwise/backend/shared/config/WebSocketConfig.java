package com.boardwise.backend.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.boardwise.backend.shared.security.JWTService;
import com.boardwise.backend.user_service.services.MyUserDetailsService;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JWTService jwtService;
    private final MyUserDetailsService userDetailsService;

    public WebSocketConfig(
        JWTService jwtService,
        MyUserDetailsService userDetailsService
    ){
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/api/stomp")
            // .setAllowedOriginPatterns("http://localhost:3000");
            .setAllowedOriginPatterns("*");
            // .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            public Message<?> preSend(Message<?> message, MessageChannel channel){
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if(StompCommand.CONNECT.equals(accessor.getCommand())){
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if(authHeader != null && authHeader.startsWith("Bearer ")){
                        String token = authHeader.substring(7);
                
                        try{
                            String userId = jwtService.extractUserId(token).toString();
                            UserDetails deets = userDetailsService.loadUserByUserId(userId);
                            if(jwtService.validateToken(token, deets))
                                accessor.setUser(() -> userId);
                            else
                                throw new Exception("JWT token is invalid.");

                            
                        } catch(Exception e){
                            throw new MessageDeliveryException(
                                "Unauthorised WebSocket connection. Reason: " +
                                e.getMessage()
                            );
                        }
                    }
                    else{
                        throw new MessageDeliveryException(
                            "Unauthorised WebSocket connection. Reason: " +
                            "Authorization header missing or not correctly formatted."
                        );
                    }
                }

                return message;
            }
        });
    }
}
