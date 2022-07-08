package de.kaliburg.morefair.api.websockets;

import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.game.round.RoundUtils;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class TopicSubscriptionInterceptor implements ChannelInterceptor {

  private final AccountService accountService;
  private final RoundService roundService;

  private final RoundUtils roundUtils;

  public TopicSubscriptionInterceptor(AccountService accountService, RoundService roundService,
      RoundUtils roundUtils) {
    this.accountService = accountService;
    this.roundService = roundService;
    this.roundUtils = roundUtils;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
    if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
      log.trace("Payload: {} Headers: {}",
          new String((byte[]) message.getPayload(), StandardCharsets.UTF_8),
          message.getHeaders().toString());
      Principal userPrincipal = headerAccessor.getUser();
      String uuid = Objects.requireNonNull(headerAccessor.getNativeHeader("uuid")).get(0);
      if (!validateSubscription(userPrincipal, headerAccessor.getDestination(), uuid)) {
        throw new MessagingException(
            "No permission for this topic (" + StringEscapeUtils.escapeJava(
                headerAccessor.getDestination()) + ") with principal: " + userPrincipal);
      }
    }
    return message;
  }

  private boolean validateSubscription(Principal principal, String topicDestination,
      String uuid) {
    if (principal == null) {
      return false;
    }
    topicDestination = StringEscapeUtils.escapeJava(topicDestination);
    uuid = StringEscapeUtils.escapeJava(uuid);

    log.debug("Validate subscription for {} to {}", uuid, topicDestination);

    // Give free pass for all moderators and above
    if (topicDestination.contains("/topic/")) {
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account != null) {
        if (account.isMod()) {
          return true;
        }
        if (account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
          return false;
        }
      }
      // Otherwise, just normal approval
    }

    if (topicDestination.contains("/topic/chat/events/")) {
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null) {
        return false;
      }
      int chatDestination = Integer.parseInt(
          topicDestination.substring("/topic/chat/events/".length()));
      int highestLadder = account.getCurrentRankers().stream()
          .mapToInt(v -> v.getLadder().getNumber()).max().orElse(1);
      if (chatDestination > highestLadder) {
        return false;
      }
    }

    if (topicDestination.contains("/topic/game/events/")) {
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null) {
        return false;
      }
      int ladderDestination = Integer.parseInt(
          topicDestination.substring("/topic/game/events/".length()));
      if (ladderDestination == roundUtils.getAssholeLadderNumber(roundService.getCurrentRound())) {
        return true;
      }
      int highestLadder = account.getCurrentRankers().stream()
          .mapToInt(v -> v.getLadder().getNumber())
          .max().orElse(1);
      if (ladderDestination > highestLadder) {
        return false;
      }
    }

    if (topicDestination.contains("/topic/mod/")) {
      return false;
    }

    return true;
  }
}
