package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountAccessRole;
import de.kaliburg.morefair.account.AccountDetailsDto;
import de.kaliburg.morefair.account.AccountEntity;
import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.AchievementsEntity;
import de.kaliburg.morefair.api.utils.RequestThrottler;
import de.kaliburg.morefair.api.utils.WsUtils;
import de.kaliburg.morefair.api.websockets.UserPrincipal;
import de.kaliburg.morefair.api.websockets.messages.WsMessage;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.types.EventType;
import de.kaliburg.morefair.game.round.RankerService;
import de.kaliburg.morefair.game.round.RoundEntity;
import de.kaliburg.morefair.game.round.RoundService;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
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
  private final RankerService rankerService;

  public AccountController(AccountService accountService,
      RequestThrottler requestThrottler, WsUtils wsUtils,
      RoundService roundService, RankerService rankerService) {
    this.accountService = accountService;
    this.requestThrottler = requestThrottler;
    this.wsUtils = wsUtils;
    this.roundService = roundService;
    this.rankerService = rankerService;
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
      String uuid = wsMessage.getUuid();

      log.trace("/app/{} {}", QUEUE_LOGIN_DESTINATION, uuid);

      AccountEntity account = accountService.find(UUID.fromString(uuid));

      // BANNED Players
      if (account.getAccessRole().equals(AccountAccessRole.BANNED_PLAYER)) {
        wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      }

      if (account.getAchievements() == null) {
        account.setAchievements(new AchievementsEntity(account));
        accountService.save(account);
      }

      RoundEntity currentRound = roundService.getCurrentRound();
      account = accountService.login(account, principal);
      int highestLadder = rankerService.findCurrentRankersOfAccount(account, currentRound).stream()
          .mapToInt(r -> r.getLadder().getNumber()).max().orElse(1);

      wsUtils.convertAndSendToUser(sha, QUEUE_LOGIN_DESTINATION, new AccountDetailsDto(account,
          highestLadder));


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

      String uuid = wsMessage.getUuid();

      AccountEntity account = accountService.find(UUID.fromString(uuid));
      if (account == null || account.isMuted()) {
        return;
      }

      log.info("[G] RENAME: {} (#{}) -> {}", account.getDisplayName(), account.getId(), username);

      account.setDisplayName(username);
      accountService.save(account);

      wsUtils.convertAndSendToTopic(GameController.TOPIC_GLOBAL_EVENTS_DESTINATION,
          new Event(EventType.NAME_CHANGE, account.getId(),
              account.getDisplayName()));

    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

}
