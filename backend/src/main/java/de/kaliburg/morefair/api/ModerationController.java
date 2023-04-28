package de.kaliburg.morefair.api;

import de.kaliburg.morefair.FairConfig;
import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.data.ModChatData;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.AccountEventTypes;
import de.kaliburg.morefair.game.chat.ChatService;
import de.kaliburg.morefair.game.chat.MessageEntity;
import de.kaliburg.morefair.game.chat.MessageService;
import de.kaliburg.morefair.game.round.LadderService;
import de.kaliburg.morefair.security.SecurityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
public class ModerationController {

  public static final String TOPIC_EVENTS_DESTINATION = "/mod/events";
  public static final String QUEUE_CHAT_INIT_DESTINATION = "/mod/chat/init";
  private static final String APP_CHAT_INIT_DESTINATION = "/mod/chat/init";
  private static final String APP_BAN_DESTINATION = "/mod/ban/{id}";
  private static final String APP_MUTE_DESTINATION = "/mod/mute/{id}";
  private static final String APP_FREE_DESTINATION = "/mod/free/{id}";
  private static final String APP_RENAME_DESTINATION = "/mod/rename/{id}";
  private static final String APP_PROMPT_DESTINATION = "/mod/prompt/{id}";
  private static final String APP_MOD_DESTINATION = "/mod/mod/{id}";
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

  @GetMapping("/mod/chat")
  public ResponseEntity<?> chat(SimpMessageHeaderAccessor sha, Authentication authentication)
      throws Exception {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !(account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR) || account.getAccessRole()
          .equals(AccountAccessRole.OWNER))) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      } else {
        ArrayList<MessageEntity> messages = messageService.getAllMessages();
        return new ResponseEntity<>(new ModChatData(messages,
            config), HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping(APP_BAN_DESTINATION)
  public void ban(Authentication authentication, @DestinationVariable("id") Long id) {
    try {

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.BANNED_PLAYER);
      target.setDisplayName("BANNED");
      target = accountService.save(target);
      chatService.deleteMessagesOfAccount(target);
      log.info("{} (#{}) is banning the account {} (#{})", account.getDisplayName(),
          account.getId(),
          target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.BAN, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_MUTE_DESTINATION)
  public void mute(Authentication authentication, @DestinationVariable("id") Long id) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.MUTED_PLAYER);
      target.setDisplayName(target.getDisplayName() + "(MUTED)");
      target = accountService.save(target);
      chatService.deleteMessagesOfAccount(target);
      log.info("{} (#{}) is muting the account {} (#{})", account.getDisplayName(), account.getId(),
          target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.MUTE, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_FREE_DESTINATION)
  public void free(Authentication authentication, @DestinationVariable("id") Long id) {
    try {
      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.PLAYER);
      target = accountService.save(target);
      log.info("{} (#{}) is freeing the account {} (#{})", account.getDisplayName(),
          account.getId(),
          target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.FREE, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_RENAME_DESTINATION)
  public void changeName(Authentication authentication, WsMessage wsMessage,
      @DestinationVariable("id") Long id) {
    try {
      String username = wsMessage.getContent();
      username = username.trim();
      if (username.length() > 64) {
        username = username.substring(0, 64);
      }
      username = username;

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setDisplayName(username);
      target = accountService.save(target);
      log.info("{} (#{}) is renaming the account {} (#{}) to {}", account.getDisplayName(),
          account.getId(), target.getDisplayName(), target.getId(), username);
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.NAME_CHANGE, target.getId(), username));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_PROMPT_DESTINATION)
  public void promptConfirm(Authentication authentication, WsMessage wsMessage,
      @DestinationVariable("id") Long id) {
    try {
      String text = wsMessage.getContent();

      if (text == null) {
        throw new IllegalArgumentException();
      }

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.MOD, target.getId(), text));
      log.info("{} (#{}) is prompting the account {} (#{}) with {}", account.getDisplayName(),
          account.getId(), target.getDisplayName(), target.getId(), text);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_MOD_DESTINATION)
  public void mod(Authentication authentication, @DestinationVariable("id") Long id) {
    try {

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isOwner()) {
        return;
      }

      AccountEntity target = accountService.find(id);
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessRole.MODERATOR);
      target = accountService.save(target);
      log.info("{} (#{}) is modding the account {} (#{})", account.getDisplayName(),
          account.getId(), target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.MOD, target.getId(), account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @GetMapping(value = "/mod/search/user", produces = "application/json")
  public ResponseEntity<Map<Long, String>> searchUsername(
      Authentication authentication,
      @RequestParam("name") String name) {
    try {

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      List<AccountEntity> accountsWithName = accountService.findByDisplayName(name);

      Map<Long, String> result = accountsWithName.stream()
          .collect(Collectors.toMap(AccountEntity::getId, AccountEntity::getDisplayName));
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/mod/search/alts", produces = "application/json")
  public ResponseEntity<Map<Long, String>> searchAlts(Authentication authentication,
      @RequestParam("id") String id) {
    try {

      AccountEntity account = accountService.find(SecurityUtils.getUuid(authentication));
      if (account == null || !account.isMod()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      AccountEntity target = accountService.find(Long.parseLong(id));
      List<AccountEntity> accountsWithIp = accountService.searchByIp(target.getLastIp());
      Map<Long, String> result = accountsWithIp.stream().collect(Collectors.toMap(
          AccountEntity::getId, AccountEntity::getDisplayName));

      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
