package de.kaliburg.morefair.api.websockets;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.game.ranker.services.RankerService;
import de.kaliburg.morefair.game.round.model.RoundEntity;
import de.kaliburg.morefair.game.round.services.RoundService;
import de.kaliburg.morefair.game.round.services.RoundUtils;
import de.kaliburg.morefair.security.SecurityUtils;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
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
  private final RankerService rankerService;
  private final RoundService roundService;
  private final RoundUtils roundUtils;

  public TopicSubscriptionInterceptor(AccountService accountService, RankerService rankerService,
      RoundService roundService,
      RoundUtils roundUtils) {
    this.accountService = accountService;
    this.rankerService = rankerService;
    this.roundService = roundService;
    this.roundUtils = roundUtils;
  }

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
    if (StompCommand.SUBSCRIBE.equals(headerAccessor.getCommand())) {
      log.trace("Payload: {} Headers: {}",
          new String((byte[]) message.getPayload(), StandardCharsets.UTF_8),
          message.getHeaders().toString());
      Principal userPrincipal = headerAccessor.getUser();
      UUID uuid = SecurityUtils.getUuid(userPrincipal);
      if (!validateSubscription(userPrincipal, headerAccessor.getDestination(), uuid)) {
        throw new MessagingException(
            "No permission for this topic (" + headerAccessor.getDestination()
                + ") with principal: " + userPrincipal);
      }
    }
    return message;
  }

  private boolean validateSubscription(Principal principal, String topicDestination, UUID uuid) {
    if (principal == null) {
      return false;
    }

    log.trace("Validate subscription for {} to {}", uuid, topicDestination);

    if (!topicDestination.contains("/topic/")) {
      return true;
    }

    AccountEntity account = accountService.find(uuid);

    if (account == null) {
      return false;
    }

    if (account.isMod()) {
      return true;
    }
    if (account.isBanned()) {
      return false;
    }

    RoundEntity currentRound = roundService.getCurrentRound();

    if (topicDestination.contains("/topic/chat/events/ladder/")) {

      int chatDestination = Integer.parseInt(
          topicDestination.substring("/topic/chat/events/ladder/".length()));
      int highestLadder = rankerService.findCurrentRankersOfAccount(account, currentRound).stream()
          .mapToInt(v -> v.getLadderId().getNumber()).max().orElse(1);
      if (chatDestination > highestLadder) {
        return false;
      }
    }

    if (topicDestination.contains("/topic/game/events/")) {
      int ladderDestination = Integer.parseInt(
          topicDestination.substring("/topic/game/events/".length()));
      if (ladderDestination == roundUtils.getAssholeLadderNumber(roundService.getCurrentRound())) {
        return true;
      }
      int highestLadder = rankerService.findCurrentRankersOfAccount(account, currentRound).stream()
          .mapToInt(v -> v.getLadderId().getNumber())
          .max().orElse(1);
      if (ladderDestination > highestLadder) {
        return false;
      }
    }

    return !topicDestination.contains("/topic/moderation/");
  }
}
