package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsEmptyMessage;
import de.kaliburg.morefair.api.websockets.messages.WsMetaMessage;
import de.kaliburg.morefair.game.GameService;
import de.kaliburg.morefair.game.chat.ChatDTO;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.MessageDTO;
import de.kaliburg.morefair.game.chat.MessageEntity;
import de.kaliburg.morefair.game.chat.MessageService;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
public class ChatController {

  public static final String CHAT_INIT_DESTINATION = "/chat/init/";
  public static final String CHAT_UPDATE_DESTINATION = "/chat/updates/";
  private final MessageService messageService;
  private final AccountService accountService;
  private final RankerService rankerService;
  private final WsUtils wsUtils;
  private final RequestThrottler throttler;
  private final RoundService roundService;
  private final LadderService ladderService;
  private final ChatService chatService;
  private final GameService gameService;

  public ChatController(MessageService messageService, AccountService accountService,
      RankerService rankerService,
      WsUtils wsUtils, RequestThrottler throttler, RoundService roundService,
      LadderService ladderService, ChatService chatService, GameService gameService) {
    this.messageService = messageService;
    this.accountService = accountService;
    this.rankerService = rankerService;
    this.wsUtils = wsUtils;
    this.throttler = throttler;
    this.roundService = roundService;
    this.ladderService = ladderService;
    this.chatService = chatService;
    this.gameService = gameService;
  }

  @MessageMapping(CHAT_INIT_DESTINATION + "{number}")
  public void initChat(SimpMessageHeaderAccessor sha, WsEmptyMessage wsMessage,
      @DestinationVariable("number") Integer number) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.trace("/app/chat/init/{} from {}", number, uuid);
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isBanned()) {
        wsUtils.convertAndSendToUser(sha, CHAT_INIT_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      }

      RankerEntity ranker = rankerService.findHighestActiveRankerOfAccount(account);
      if (ranker == null) {
        ranker = roundService.createNewRanker(account);
      }

      if (account.isMod() || number <= ranker.getLadder().getNumber()) {
        ChatDTO c = new ChatDTO(chatService.getChat(number));
        wsUtils.convertAndSendToUser(sha, CHAT_INIT_DESTINATION, c);
      } else {
        wsUtils.convertAndSendToUser(sha, CHAT_INIT_DESTINATION, HttpStatus.FORBIDDEN);
      }

    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_INIT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_INIT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/chat/post/{number}")
  public void postChat(WsMetaMessage wsMessage, @DestinationVariable("number") Integer number) {
    try {
      String message = wsMessage.getContent();
      String metadata = wsMessage.getMetadata();
      message = message.trim();
      if (message.length() > 140) {
        message = message.substring(0, 140);
      }
      message = StringEscapeUtils.escapeJava(message);

      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isMuted()) {
        return;
      }
      RankerEntity ranker = rankerService.findHighestActiveRankerOfAccount(account);
      if (account.isMod() || (number <= ranker.getLadder().getNumber() && throttler.canPostMessage(
          account))) {
        MessageEntity answer = chatService.sendMessageToChat(account, number, message, metadata);
        wsUtils.convertAndSendToTopic(CHAT_UPDATE_DESTINATION + number, new MessageDTO(answer));
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
