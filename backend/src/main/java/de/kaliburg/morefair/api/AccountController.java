package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountDetailsDto;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.UserPrincipal;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Log4j2
public class AccountController {

  private static final String APP_LOGIN_DESTINATION = "/account/login";
  private static final String APP_RENAME_DESTINATION = "/account/name";
  private static final String QUEUE_LOGIN_DESTINATION = "/account/login";

  private final AccountService accountService;
  private final RequestThrottler requestThrottler;
  private final WsUtils wsUtils;
  private final RoundService roundService;

  public AccountController(AccountService accountService,
      RequestThrottler requestThrottler, WsUtils wsUtils,
      RoundService roundService) {
    this.accountService = accountService;
    this.requestThrottler = requestThrottler;
    this.wsUtils = wsUtils;
    this.roundService = roundService;
  }

  /**
   * This websocket is used to
   *
   * @param sha
   * @param wsMessage
   */
  @MessageMapping(APP_LOGIN_DESTINATION)
  public void login(SimpMessageHeaderAccessor sha, WsMessage wsMessage) {
    try {
      UserPrincipal principal = wsUtils.convertMessageHeaderAccessorToUserPrincipal(sha);
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      log.trace("/app/{} {}", QUEUE_LOGIN_DESTINATION, uuid);

      RoundEntity currentRound = roundService.getCurrentRound();

      // Empty UUID
      if (uuid == null || uuid.isBlank()) {
        if (requestThrottler.canCreateAccount(principal)) {
          wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION,
              new AccountDetailsDto(accountService.create(principal), currentRound),
              HttpStatus.CREATED);
        } else {
          wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
        }
        return;
      }

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      // Can't find account with valid UUID
      if (account == null) {
        if (requestThrottler.canCreateAccount(principal)) {
          wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION,
              new AccountDetailsDto(accountService.create(principal), currentRound),
              HttpStatus.CREATED);
        } else {
          wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
        }
        return;
      }

      // BANNED Players
      if (account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
        wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
      }

      account = accountService.login(account, principal);
      wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, new AccountDetailsDto(account,
          currentRound));


    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION,
          HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(APP_RENAME_DESTINATION)
  public void changeUsername(SimpMessageHeaderAccessor sha, WsMessage wsMessage) {
    try {
      String username = wsMessage.getContent();
      username = username.trim();
      if (username.length() > 32) {
        username = username.substring(0, 32);
      }
      username = StringEscapeUtils.escapeJava(username);

      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.trace("/app/{} {} {}", APP_RENAME_DESTINATION, uuid, username);

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isMuted()) {
        return;
      }
      account.setUsername(username);
      accountService.save(account);

      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION,
          new Event(EventType.NAME_CHANGE, account.getId(), account.getUsername()));

    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
