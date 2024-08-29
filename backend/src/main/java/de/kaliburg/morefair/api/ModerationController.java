package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.model.AccountEntity;
import de.kaliburg.morefair.account.model.types.AccountAccessType;
import de.kaliburg.morefair.account.services.AccountService;
import de.kaliburg.morefair.api.utils.HttpUtils;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.chat.model.MessageEntity;
import de.kaliburg.morefair.chat.model.types.ChatType;
import de.kaliburg.morefair.chat.services.MessageService;
import de.kaliburg.morefair.chat.services.mapper.ChatMapper;
import de.kaliburg.morefair.data.ModChatDto;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.AccountEventTypes;
import de.kaliburg.morefair.security.SecurityUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Log4j2
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

  public static final String TOPIC_LOG_EVENTS_DESTINATION = "/moderation/log/events";
  public static final String TOPIC_CHAT_EVENTS_DESTINATION = "/moderation/chat/events";
  private static final String APP_BAN_DESTINATION = "/moderation/ban/{id}";
  private static final String APP_MUTE_DESTINATION = "/moderation/mute/{id}";
  private static final String APP_FREE_DESTINATION = "/moderation/free/{id}";
  private static final String APP_RENAME_DESTINATION = "/moderation/rename/{id}";
  private static final String APP_PROMPT_DESTINATION = "/moderation/prompt/{id}";
  private static final String APP_MOD_DESTINATION = "/moderation/mod/{id}";
  private final AccountService accountService;
  private final WsUtils wsUtils;
  private final MessageService messageService;
  private final ChatMapper chatMapper;

  @GetMapping("/chat")
  public ResponseEntity<?> getChat(Authentication authentication) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      } else {
        List<MessageEntity> messages =
            messageService.findNewestMessagesByChatType(List.of(ChatType.LADDER, ChatType.GLOBAL,
                ChatType.SYSTEM, ChatType.MOD));

        ModChatDto modChatDto = chatMapper.mapToModChatDto(messages);
        return new ResponseEntity<>(modChatDto, HttpStatus.OK);
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return HttpUtils.buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @MessageMapping(APP_BAN_DESTINATION)
  public void ban(Authentication authentication, @DestinationVariable("id") Long id) {
    try {

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.findById(id).orElseThrow();

      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessType.BANNED_PLAYER);
      target.setDisplayName("[BANNED]");
      target = accountService.save(target);
      messageService.deleteMessagesOfAccount(target);
      log.info("[MOD] {} (#{}) is banning the account {} (#{})", account.getDisplayName(),
          account.getId(),
          target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.BAN, target.getId()));
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.NAME_CHANGE, target.getId(), target.getDisplayName()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_MUTE_DESTINATION)
  public void mute(Authentication authentication, @DestinationVariable("id") Long id) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.findById(id).orElseThrow();
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessType.MUTED_PLAYER);
      target.setDisplayName("[MUTED]" + target.getDisplayName());
      target = accountService.save(target);
      messageService.deleteMessagesOfAccount(target);
      log.info("[MOD] {} (#{}) is muting the account {} (#{})", account.getDisplayName(),
          account.getId(),
          target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.MUTE, target.getId()));
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.NAME_CHANGE, target.getId(), target.getDisplayName()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_FREE_DESTINATION)
  public void free(Authentication authentication, @DestinationVariable("id") Long id) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.findById(id).orElseThrow();
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessType.PLAYER);
      target = accountService.save(target);
      log.info("[MOD] {} (#{}) is freeing the account {} (#{})", account.getDisplayName(),
          account.getId(),
          target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.FREE, target.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_RENAME_DESTINATION)
  public void changeName(Authentication authentication, String displayName,
      @DestinationVariable("id") Long id) {
    try {
      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return;
      }

      displayName = displayName.trim();
      if (displayName.length() > 64) {
        displayName = displayName.substring(0, 32);
      }

      if (displayName.isBlank()) {
        return;
      }

      AccountEntity target = accountService.findById(id).orElseThrow();
      if (target.isOwner()) {
        return;
      }
      target.setDisplayName(displayName);
      target = accountService.save(target);
      log.info("[MOD] {} (#{}) is renaming the account {} (#{}) to {}", account.getDisplayName(),
          account.getId(), target.getDisplayName(), target.getId(), displayName);
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.NAME_CHANGE, target.getId(), displayName));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
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

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return;
      }

      AccountEntity target = accountService.findById(id).orElseThrow();
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION,
          new Event<>(AccountEventTypes.MOD, target.getId(), text));
      log.info("[MOD] {} (#{}) is prompting the account {} (#{}) with {}", account.getDisplayName(),
          account.getId(), target.getDisplayName(), target.getId(), text);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @MessageMapping(APP_MOD_DESTINATION)
  public void mod(Authentication authentication, @DestinationVariable("id") Long id) {
    try {

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isOwner()) {
        return;
      }

      AccountEntity target = accountService.findById(id).orElseThrow();
      if (target.isOwner()) {
        return;
      }
      target.setAccessRole(AccountAccessType.MODERATOR);
      target = accountService.save(target);
      log.info("[MOD] {} (#{}) is modding the account {} (#{})", account.getDisplayName(),
          account.getId(), target.getDisplayName(), target.getId());
      wsUtils.convertAndSendToTopic(AccountController.TOPIC_EVENTS_DESTINATION, new Event<>(
          AccountEventTypes.MOD, target.getId(), account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @GetMapping(value = "/search/user", produces = "application/json")
  public ResponseEntity<Map<Long, String>> searchUsername(
      Authentication authentication,
      @RequestParam("username") String name) {
    try {

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      List<AccountEntity> accountsWithName = accountService.findByDisplayName(name);

      Map<Long, String> result = accountsWithName.stream()
          .collect(Collectors.toMap(AccountEntity::getId, AccountEntity::getDisplayName));
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(value = "/search/alts", produces = "application/json")
  public ResponseEntity<?> searchAlts(Authentication authentication,
      @RequestParam("accountId") String accountId) {
    try {

      AccountEntity account = accountService.findByUuid(SecurityUtils.getUuid(authentication))
          .orElse(null);
      if (account == null || !account.isMod()) {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
      }
      AccountEntity target = accountService.findById(Long.parseLong(accountId)).orElse(null);
      if (target == null) {
        return new ResponseEntity<>(new HashMap<Long, String>(), HttpStatus.OK);
      }
      List<AccountEntity> accountsWithIp = accountService.searchByIp(target.getLastIp());
      Map<Long, String> result = accountsWithIp.stream().collect(Collectors.toMap(
          AccountEntity::getId, AccountEntity::getDisplayName));

      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return ResponseEntity.internalServerError().body(e.getMessage());
    }
  }
}
