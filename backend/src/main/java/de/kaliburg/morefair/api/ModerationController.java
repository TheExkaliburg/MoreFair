package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.data.ModChatData;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.MessageEntity;
import de.kaliburg.morefair.game.chat.MessageService;
import de.kaliburg.morefair.game.round.LadderService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
public class ModerationController {

  public static final String APP_CHAT_INIT_DESTINATION = "/mod/chat/init";
  public static final String APP_BAN_DESTINATION = "/mod/ban/{id}";
  public static final String APP_MUTE_DESTINATION = "/mod/mute/{id}";
  public static final String APP_FREE_DESTINATION = "/mod/free/{id}";
  public static final String APP_RENAME_DESTINATION = "/mod/rename/{id}";
  public static final String APP_PROMPT_DESTINATION = "/mod/prompt/{id}";
  public static final String APP_MOD_DESTINATION = "/mod/mod/{id}";
  public static final String TOPIC_CHAT_EVENTS_DESTINATION = "/mod/chat/events";
  public static final String TOPIC_EVENTS_DESTINATION = "/mod/events";
  public static final String QUEUE_CHAT_INIT_DESTINATION = "/mod/chat/init";

  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final LadderService ladderService;
  private final MessageService messageService;
  private final ChatService chatService;
  private final FairConfig config;

  public ModerationController(AccountService accountService, WsUtils wsUtils,
      LadderService ladderService,
      MessageService messageService, ChatService chatService, FairConfig config) {
    this.accountService = accountService;
    this.wsUtils = wsUtils;
    this.ladderService = ladderService;
    this.messageService = messageService;
    this.chatService = chatService;
    this.config = config;
  }

  /*@MessageMapping(INFO_REQUEST)
  public void info(SimpMessageHeaderAccessor sha, WsMessage wsMessage) throws Exception {
    try {
      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole().equals(AccountAccessRole.MODERATOR)
          || account.getAccessRole().equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, INFO_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        Integer highestLadderNumber = ladderService.getHighestLadder().getNumber();
        ModerationInfoData infoData = new ModerationInfoData(highestLadderNumber,
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
  }*/

  @MessageMapping(APP_CHAT_INIT_DESTINATION)
  public void chat(SimpMessageHeaderAccessor sha, WsMessage wsMessage) throws Exception {
    try {
      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        wsUtils.convertAndSendToUser(sha, QUEUE_CHAT_INIT_DESTINATION, HttpStatus.FORBIDDEN);
      } else {
        ArrayList<MessageEntity> messages = messageService.getAllMessages();
        wsUtils.convertAndSendToUser(sha, QUEUE_CHAT_INIT_DESTINATION, new ModChatData(messages,
            config));
      }
    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_CHAT_INIT_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_CHAT_INIT_DESTINATION,
          HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_BAN_DESTINATION)
  public void ban(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.BANNED_PLAYER);
      target.setUsername("BANNED");
      target = accountService.save(target);
      log.info("{} (#{}) is banning the account {} (#{})", account.getUsername(), account.getId(),
          target.getUsername(), target.getId());
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.BAN, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_MUTE_DESTINATION)
  public void mute(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.MUTED_PLAYER);
      target.setUsername(target.getUsername() + "(MUTED)");
      target = accountService.save(target);
      log.info("{} (#{}) is muting the account {} (#{})", account.getUsername(), account.getId(),
          target.getUsername(), target.getId());
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.MUTE, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_FREE_DESTINATION)
  public void free(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id) {
    try {
      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.PLAYER);
      target = accountService.save(target);
      log.info("{} (#{}) is freeing the account {} (#{})", account.getUsername(), account.getId(),
          target.getUsername(), target.getId());
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.FREE, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_RENAME_DESTINATION)
  public void changeName(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String username = wsMessage.getContent();
      username = username.trim();
      if (username.length() > 64) {
        username = username.substring(0, 64);
      }
      username = username;

      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setUsername(username);
      target = accountService.save(target);
      log.info("{} (#{}) is renaming the account {} (#{}) to {}", account.getUsername(),
          account.getId(), target.getUsername(), target.getId(), username);
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.NAME_CHANGE, target.getId(), username));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_PROMPT_DESTINATION)
  public void promptConfirm(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id) {
    try {
      String uuid = wsMessage.getUuid();
      String text = wsMessage.getContent();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.MOD, target.getId(), text));
      log.info("{} (#{}) is prompting the account {} (#{}) with {}", account.getUsername(),
          account.getId(), target.getUsername(), target.getId(), text);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_MOD_DESTINATION)
  public void mod(SimpMessageHeaderAccessor sha, WsMessage wsMessage,
      @DestinationVariable("id") Long id)
      throws Exception {
    try {
      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isOwner()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.MODERATOR);
      target = accountService.save(target);
      log.info("{} (#{}) is modding the account {} (#{})", account.getUsername(),
          account.getId(), target.getUsername(), target.getId());
      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION, new Event(
          EventType.MOD, target.getId(), account.getId()));
    } catch (Exception e) {
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

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || !account.isMod()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      List<AccountEntity> accountsWithName = accountService.findByUsername(name);

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
