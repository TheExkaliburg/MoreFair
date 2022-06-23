package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.UserPrincipal;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.dto.AccountDetailsDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.GameService;
import de.kaliburg.morefair.game.ranker.RankerService;
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

  private static final String LOGIN_DESTINATION = "/account/login/";
  private static final String RENAME_DESTINATION = "/account/name/";

  private final AccountService accountService;
  private final GameService gameService;
  private final RequestThrottler requestThrottler;
  private final WsUtils wsUtils;

  public AccountController(AccountService accountService, RankerService rankerService,
      GameService gameService, RequestThrottler requestThrottler, WsUtils wsUtils) {
    this.accountService = accountService;
    this.gameService = gameService;
    this.requestThrottler = requestThrottler;
    this.wsUtils = wsUtils;
  }

  /**
   * This websocket is used to
   *
   * @param sha
   * @param wsMessage
   */
  @MessageMapping(LOGIN_DESTINATION)
  public void login(SimpMessageHeaderAccessor sha, WsMessage wsMessage) {
    try {
      UserPrincipal principal = wsUtils.convertMessageHeaderAccessorToUserPrincipal(sha);
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());

      // Empty UUID
      if (uuid == null || uuid.isBlank()) {
        if (requestThrottler.canCreateAccount(principal)) {
          wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION,
              accountService.create(principal), HttpStatus.CREATED);
        } else {
          wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
        }
        return;
      }

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      // Can't find account with valid UUID
      if (account == null) {
        if (requestThrottler.canCreateAccount(principal)) {
          wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION,
              accountService.create(principal),
              HttpStatus.CREATED);
        } else {
          wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
        }
        return;
      }

      // BANNED Players
      if (account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
        wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
      }

      account = accountService.login(account, principal);
      wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, new AccountDetailsDTO(account));


    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, LOGIN_DESTINATION,
          HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping(RENAME_DESTINATION)
  public void changeUsername(SimpMessageHeaderAccessor sha, WsMessage wsMessage) {
    try {
      String username = wsMessage.getContent();
      username = username.trim();
      if (username.length() > 32) {
        username = username.substring(0, 32);
      }
      username = StringEscapeUtils.escapeJava(username);

      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.debug("/app/account/name {} {}", uuid, username);

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.MUTED_PLAYER) || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Event event = new Event(EventType.NAME_CHANGE, account.getId());
      event.setData(username);
      gameService.addEvent(account, event);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
