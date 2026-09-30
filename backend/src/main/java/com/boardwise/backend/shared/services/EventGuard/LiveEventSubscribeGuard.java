package com.boardwise.backend.shared.services.EventGuard;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.boardwise.backend.user_service.repository.LiveEventRepository;

@Component
public class LiveEventSubscribeGuard implements ChannelInterceptor {

    private static final Pattern TOPIC =
            Pattern.compile("^/topic/live-event/([a-f0-9]{24})/(messages|roster)$");

    private final LiveEventRepository liveEventRepo;

    public LiveEventSubscribeGuard(LiveEventRepository liveEventRepo) {
        this.liveEventRepo = liveEventRepo;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null || !StompCommand.SUBSCRIBE.equals(acc.getCommand())) return message;

        String dest = acc.getDestination();
        if (dest == null || !dest.startsWith("/topic/live-event/")) return message;

        Principal p = acc.getUser();
        if (p == null) throw new MessageDeliveryException("Not authenticated");

        Matcher m = TOPIC.matcher(dest);
        if (!m.matches()) throw new MessageDeliveryException("Bad destination");

        if (m.group(2).equals("messages") && !isAttendee(p.getName(), m.group(1))) {
            throw new MessageDeliveryException("Not in this event");
        }
        return message;
    }

    private boolean isAttendee(String userId, String eventId) {
        return liveEventRepo.findById(eventId)
                .map(e -> e.getLiveAttendees().attendees().stream()
                        .anyMatch(a -> a.userId().equals(userId)))
                .orElse(false);
    }
}