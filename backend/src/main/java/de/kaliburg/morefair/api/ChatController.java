package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsEmptyMessage;
import de.kaliburg.morefair.api.websockets.messages.WsMetaMessage;
import de.kaliburg.morefair.game.chat.ChatDto;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.MessageDto;
import de.kaliburg.morefair.game.chat.MessageEntity;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
public class ChatController {

  public static final String TOPIC_EVENTS_DESTINATION = "/chat/events/{number}";

  public static final String QUEUE_INIT_DESTINATION = "/chat/init";
  public static final String PRIVATE_PROMPT_DESTINATION = "/chat/prompt";

  public static final String APP_INIT_DESTINATION = "/chat/init/{number}";
  public static final String APP_CHAT_DESTINATION = "/chat/{number}";

  private final AccountService accountService;
  private final RankerService rankerService;
  private final WsUtils wsUtils;
  private final RequestThrottler throttler;
  private final RoundService roundService;
  private final ChatService chatService;
  private final FairConfig config;

  public ChatController(AccountService accountService, RankerService rankerService, WsUtils wsUtils,
      RequestThrottler throttler, RoundService roundService, ChatService chatService,
      FairConfig config) {
    this.accountService = accountService;
    this.rankerService = rankerService;
    this.wsUtils = wsUtils;
    this.throttler = throttler;
    this.roundService = roundService;
    this.chatService = chatService;
    this.config = config;
  }

  @MessageMapping(APP_INIT_DESTINATION)
  public void initChat(SimpMessageHeaderAccessor sha, WsEmptyMessage wsMessage,
      @DestinationVariable("number") Integer number) {
    try {
      String uuid = wsMessage.getUuid();
      log.trace("/app/chat/init/{} from {}", number, uuid);
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      }

      RoundEntity currentRound = roundService.getCurrentRound();

      RankerEntity ranker = rankerService.findHighestActiveRankerOfAccountAndRound(account,
          currentRound);
      if (ranker == null) {
        ranker = roundService.createNewRanker(account);
      }

      if (account.isMod() || number <= ranker.getLadder().getNumber()) {
        ChatDto c = new ChatDto(chatService.find(number), config);
        wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, c);
      } else {
        wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, HttpStatus.FORBIDDEN);
      }

    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_INIT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_CHAT_DESTINATION)
  public void postChat(WsMetaMessage wsMessage, @DestinationVariable("number") Integer number) {
    try {
      String message = wsMessage.getContent();
      String metadata = wsMessage.getMetadata();
      message = message.trim();
      if (message.length() > 280) {
        message = message.substring(0, 280);
      }

      String uuid = wsMessage.getUuid();
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isMuted()) {
        return;
      }
      RoundEntity currentRound = roundService.getCurrentRound();
      RankerEntity ranker = rankerService.findHighestActiveRankerOfAccountAndRound(account,
          currentRound);
      if (account.isMod() || (number <= ranker.getLadder().getNumber() && throttler.canPostMessage(
          account))) {
        MessageEntity answer = chatService.sendMessageToChat(account, number, message, metadata);
        wsUtils.convertAndSendToTopic(TOPIC_EVENTS_DESTINATION + number, new MessageDto(answer,
            config));
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
