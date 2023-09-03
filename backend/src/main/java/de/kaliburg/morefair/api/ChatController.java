package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.SuggestionDto;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.game.chat.ChatEntity;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.ChatType;
import de.kaliburg.morefair.game.chat.MessageEntity;
import de.kaliburg.morefair.game.chat.MessageService;
import de.kaliburg.morefair.game.chat.dto.ChatDto;
import de.kaliburg.morefair.game.round.LadderEntity;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.game.round.RankerEntity;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import de.kaliburg.morefair.security.SecurityUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

  public static final String PRIVATE_EVENTS_DESTINATION = "/chat/events";
  public static final String TOPIC_EVENTS_DESTINATION = "/chat/events/{number}";
  private final AccountService accountService;
  private final RankerService rankerService;
  private final WsUtils wsUtils;
  private final RequestThrottler throttler;
  private final RoundService roundService;
  private final ChatService chatService;
  private final FairConfig config;
  private final LadderService ladderService;
  private final MessageService messageService;

  @GetMapping(value = "/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getChat(
      @PathVariable("type") String typeString,
      @RequestParam(value = "number", defaultValue = "0", required = false) Integer number,
      Authentication authentication) {
    try {
      ChatType type = ChatType.valueOf(typeString.toUpperCase());

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isBanned()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

      if (type == ChatType.LADDER && number != null) {
        RankerEntity ranker = ladderService.findFirstActiveRankerOfAccountThisRound(account);
        int highestLadderNumber = ranker == null ? 1 : ranker.getLadder().getNumber();

        if (!account.isMod() && number > highestLadderNumber) {
          return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
      }

      ChatEntity chatEntity = type.isParameterized() ? chatService.find(type, number) :
          chatService.find(type);
      ChatDto c = chatService.convertToDto(chatEntity);
      return new ResponseEntity<>(c, HttpStatus.OK);
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/suggestions")
  public ResponseEntity<?> getSuggestions() {
    try {
      RoundEntity currentRound = roundService.getCurrentRound();
      LadderEntity ladder = ladderService.find(currentRound, 1);

      List<RankerEntity> rankers = ladder.getRankers();
      var allSuggestions = rankers.stream()
          .map(RankerEntity::getAccount)
          .map(a -> new SuggestionDto(a.getId(), a.getDisplayName()))
          .collect(Collectors.toList());

      return ResponseEntity.ok(allSuggestions);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping("/chat/{type}")
  public void postChat(@DestinationVariable("type") String typeString,
      @Payload WsMessage wsMessage, Authentication authentication) {
    postChat(typeString, null, wsMessage, authentication);
  }

  @MessageMapping("/chat/{type}/{number}")
  public void postChat(
      @DestinationVariable("type") String typeString,
      @DestinationVariable("number") Integer number,
      @Payload WsMessage wsMessage,
      Authentication authentication
  ) {
    try {
      ChatType type = ChatType.valueOf(typeString.toUpperCase());

      String message = wsMessage.getContent();
      String metadata = wsMessage.getMetadata();

      if (ObjectUtils.anyNull(message, metadata)) {
        log.error("message or metadata is null");
        return;
      }

      message = message.trim();
      if (message.length() > 280) {
        message = message.substring(0, 280);
      }

      if (message.isBlank()) {
        return;
      }

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || account.isMuted() || !throttler.canPostMessage(account)) {
        return;
      }

      ChatEntity chat = chatService.find(type, number);
      if (type == ChatType.LADDER && number != null) {
        RankerEntity ranker = ladderService.findFirstActiveRankerOfAccountThisRound(account);
        if (!account.isMod() && number > ranker.getLadder().getNumber()) {
          return;
        }
      }

      MessageEntity messageEntity = messageService.create(account, chat, message, metadata);
      log.info("[CHAT '{}'] {} (#{}): {}", chat.getIdentifier(), account.getDisplayName(),
          account.getId(), messageEntity.getMessage());
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
