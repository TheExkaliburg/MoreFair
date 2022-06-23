package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.data.ModChatData;
import de.kaliburg.morefair.data.ModerationInfoData;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.chat.message.MessageEntity;
import de.kaliburg.morefair.game.chat.message.MessageService;
import de.kaliburg.morefair.game.ranker.RankerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

@Controller
@Log4j2
public class ModerationController {

  public final static String CHAT_UPDATE_DESTINATION = "/topic/mod/chat/";
  public final static String GAME_UPDATE_DESTINATION = "/topic/mod/ladder/";
  public final static String GLOBAL_UPDATE_DESTINATION = "/topic/mod/global/";
  private final static String INFO_DESTINATION = "/queue/mod/info/";
  private final static String CHAT_DESTINATION = "/queue/mod/chat/";
  private final static String GAME_DESTINATION = "/queue/mod/game/";
  private final static String INFO_REQUEST = "/mod/info";
  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final RankerService ladderService;
  private final MessageService messageService;

  public ModerationController(AccountService accountService, WsUtils wsUtils,
      RankerService ladderService,
      MessageService messageService) {
    this.accountService = accountService;
    this.wsUtils = wsUtils;
    this.ladderService = ladderService;
    this.messageService = messageService;
  }

  @MessageMapping(INFO_REQUEST)
  public void info(SimpMessageHeaderAccessor sha, WsMessage wsMessage) throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        Integer highestLadder = ladderService.getHighestLadder().getNumber();
        ModerationInfoData infoData = new ModerationInfoData(highestLadder,
            account.getAccessRole());
        wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, infoData);
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/chat")
  public void chat(SimpMessageHeaderAccessor sha, WsMessage wsMessage) throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        ArrayList<MessageEntity> messages = messageService.getAllMessages();
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, new ModChatData(messages));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/ban/{id}")
  public void ban(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        log.info("{} is banning the account with id {}", account.getUsername(), id);
        accountService.addModEvent(new Event(EventType.BAN, id));
        accountService.addModEvent(new Event(EventType.NAME_CHANGE, id, "BANNED"));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/mute/{id}")
  public void mute(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        log.info("{} is muting the account with id {}", account.getUsername(), id);
        accountService.addModEvent(new Event(EventType.MUTE, id));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/free/{id}")
  public void free(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        log.info("{} is freeing the account with id {}", account.getUsername(), id);
        accountService.addModEvent(new Event(EventType.FREE, id));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/name/{id}")
  public void changeName(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      String username = wsMessage.getContent();
      username = username.trim();
      if (username.length() > 64) {
        username = username.substring(0, 64);
      }
      username = StringEscapeUtils.escapeJava(HtmlUtils.htmlEscape(username));

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      } else {
        log.info("{} is renaming the account with id {} to {}", account.getUsername(), id,
            username);
        accountService.addModEvent(new Event(EventType.NAME_CHANGE, id, username));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/confirm/{id}")
  public void promptConfirm(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      String text = StringEscapeUtils.escapeJava(
          HtmlUtils.htmlEscape(wsMessage.getContent()));

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      } else {
        log.info("{} is prompting an confirm to the account with id {} with '{}'",
            account.getUsername(), id,
            text);
        accountService.addModEvent(new Event(EventType.CONFIRM, id, text));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/mod/mod/{id}")
  public void mod(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole().equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      }
      log.info("{} is modding the account with id {}", account.getUsername(), id);
      accountService.addModEvent(new Event(EventType.MOD, id));
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, CHAT_DESTINATION, HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @GetMapping(value = "/mod/search/user", produces = "application/json")
  public ResponseEntity<Map<Long, String>> searchUsername(
      @CookieValue(name = "_uuid", defaultValue = "") String uuid,
      @RequestParam("name") String name) {
    try {
      if (uuid.isBlank()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }

      uuid = StringEscapeUtils.escapeJava(uuid);
      name = StringEscapeUtils.escapeJava(name);
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || !(account.isMod())) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      List<AccountEntity> accountsWithName = accountService.findUsername(name);

      if (accountsWithName.size() >= 100) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      Map<Long, String> result = accountsWithName.stream()
          .collect(Collectors.toMap(AccountEntity::getId, AccountEntity::getUsername));
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
