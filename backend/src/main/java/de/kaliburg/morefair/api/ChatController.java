package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMetaMessage;
import de.kaliburg.morefair.game.chat.ChatDto;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

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
  private final LadderService ladderService;

  public static String TOPIC_EVENTS_DESTINATION(Integer number) {
    return "/chat/event/{number}".replace("{number}", number.toString());
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> initChat(@RequestParam("number") Integer number,
      Authentication authentication) {
    try {
      log.trace("/app/chat/init/{} from {}", number, authentication.getName());
      AccountEntity account = accountService.find(UUID.fromString(authentication.getName()));
      if (account == null || account.isBanned()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

      RankerEntity ranker = ladderService.findFirstActiveRankerOfAccountThisRound(account);
      if (ranker == null) {
        ranker = roundService.createNewRanker(account);
      }

      if (account.isMod() || number <= ranker.getLadder().getNumber()) {
        ChatDto c = new ChatDto(chatService.find(number), config);
        return new ResponseEntity<>(c, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping(APP_CHAT_DESTINATION)
  public void postChat(WsMetaMessage wsMessage, @DestinationVariable("number") Integer number,
      Authentication authentication) {
    try {
      String message = wsMessage.getContent();
      String metadata = wsMessage.getMetadata();
      message = message.trim();
      if (message.length() > 280) {
        message = message.substring(0, 280);
      }

      if (message.isBlank()) {
        return;
      }

      AccountEntity account = accountService.find(UUID.fromString(authentication.getName()));
      if (account == null || account.isMuted()) {
        return;
      }
      RankerEntity ranker = ladderService.findFirstActiveRankerOfAccountThisRound(account);
      if (account.isMod()
          || (number <= ranker.getLadder().getNumber() && throttler.canPostMessage(account))) {
        chatService.sendMessageToChat(account, number, message, metadata);
        log.info("[CHAT {}] {} (#{}): {}", number, account.getDisplayName(), account.getId(),
            message);
      }

    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
