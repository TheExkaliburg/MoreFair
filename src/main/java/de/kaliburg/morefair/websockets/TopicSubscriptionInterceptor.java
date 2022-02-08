package de.kaliburg.morefair.websockets;

import de.kaliburg.morefair.account.entity.Account;
import de.kaliburg.morefair.account.service.AccountService;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Objects;
import java.util.UUID;

@Log4j2
@Component
public class TopicSubscriptionInterceptor implements ChannelInterceptor {
    private final AccountService accountService;

    public TopicSubscriptionInterceptor(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
            log.trace("Payload: {} Headers: {}", new String((byte[]) message.getPayload(), StandardCharsets.UTF_8), message.getHeaders().toString());
            Principal userPrincipal = headerAccessor.getUser();
            String uuid = Objects.requireNonNull(headerAccessor.getNativeHeader("uuid")).get(0);
            if (!validateSubscription(userPrincipal, headerAccessor.getDestination(), uuid)) {
                throw new MessagingException("No permission for this topic (" + StringEscapeUtils.escapeJava(headerAccessor.getDestination()) + ") with principal: " + userPrincipal);
            }
        }
        return message;
    }

    private boolean validateSubscription(Principal principal, String topicDestination, String uuid) {
        if (principal == null) return false;
        topicDestination = StringEscapeUtils.escapeJava(topicDestination);
        uuid = StringEscapeUtils.escapeJava(uuid);

        log.debug("Validate subscription for {} to {}", uuid, topicDestination);

        // Give free pass for all moderators and above
        if (topicDestination.contains("/topic/")) {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account != null) {
                if (account.getAccessRole().equals(AccountAccessRole.OWNER) || account.getAccessRole().equals(AccountAccessRole.MODERATOR)) {
                    return true;
                }
                if (account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
                    return false;
                }
            }
            // Otherwise, just normal approval
        }

        if (topicDestination.contains("/topic/chat/")) {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return false;
            int chatDestination = Integer.parseInt(topicDestination.substring("/topic/chat/".length()));
            int highestLadder = account.getRankers().stream().mapToInt(v -> v.getLadder().getNumber()).max().orElse(1);
            if (chatDestination > highestLadder) return false;
        }
        if (topicDestination.contains("/topic/ladder/")) {
            Account account = accountService.findAccountByUUID(UUID.fromString(uuid));
            if (account == null) return false;
            int ladderDestination = Integer.parseInt(topicDestination.substring("/topic/ladder/".length()));
            int highestLadder = account.getRankers().stream().mapToInt(v -> v.getLadder().getNumber()).max().orElse(1);
            if (ladderDestination > highestLadder) return false;
        }

        return true;
    }
}
