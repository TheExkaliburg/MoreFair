package de.kaliburg.morefair.websockets;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;

import java.security.Principal;

@Log4j2
public class TopicSubscriptionInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.info("preSend");

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            Principal userPrincipal = headerAccessor.getUser();
            if (!validateSubscription(userPrincipal, headerAccessor.getDestination())) {
                throw new IllegalArgumentException("No permission for this topic");
            }
        }
        return message;
    }

    private boolean validateSubscription(Principal principal, String topicDestination) {
        if (principal == null) {
            // unauthenticated user
            return false;
        }
        log.info("Validate subscription for {} to topic {}", principal.getName(), topicDestination);
        //Additional validation logic coming here
        return true;
    }
}
