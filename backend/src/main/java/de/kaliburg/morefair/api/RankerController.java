package de.kaliburg.morefair.api;

import de.kaliburg.morefair.account.AccountService;
import de.kaliburg.morefair.account.entity.AccountEntity;
import de.kaliburg.morefair.account.type.AccountAccessRole;
import de.kaliburg.morefair.api.utils.WSUtils;
import de.kaliburg.morefair.api.websockets.messages.WSMessage;
import de.kaliburg.morefair.api.websockets.messages.WSObservedMessage;
import de.kaliburg.morefair.data.ModServerMessageData;
import de.kaliburg.morefair.dto.LadderResultsDTO;
import de.kaliburg.morefair.dto.LadderViewDTO;
import de.kaliburg.morefair.events.Event;
import de.kaliburg.morefair.events.EventType;
import de.kaliburg.morefair.game.round.ranker.RankerEntity;
import de.kaliburg.morefair.game.round.ranker.RankerService;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class RankerController {

  public static final String LADDER_DESTINATION = "/queue/ladder/";
  public static final String LADDER_PRIVATE_UPDATE_DESTINATION = "/queue/ladder/updates/";
  public static final String LADDER_UPDATE_DESTINATION = "/topic/ladder/";
  public static final String GLOBAL_UPDATE_DESTINATION = "/topic/global/";
  private final RankerService rankerService;
  private final AccountService accountService;
  private final WSUtils wsUtils;

  public RankerController(RankerService rankerService, AccountService accountService,
      WSUtils wsUtils) {
    this.rankerService = rankerService;
    this.accountService = accountService;
    this.wsUtils = wsUtils;
  }

  @GetMapping(value = "/lastRound", produces = "application/json")
  public ResponseEntity<LadderResultsDTO> getStatistics() {
    try {
      if (rankerService.getLastRoundResults() == null) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      return new ResponseEntity<>(rankerService.getLastRoundResults(), HttpStatus.OK);
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @MessageMapping("/ladder/init/{number}")
  public void initLadder(SimpMessageHeaderAccessor sha, WSMessage wsMessage,
      @DestinationVariable("number") Integer number) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      log.debug("/app/ladder/init/{} from {}", number, uuid);
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.FORBIDDEN);
        return;
      }

      RankerEntity ranker = rankerService.findHighestActiveRankerByAccount(account);

      if (ranker == null) {
        rankerService.getLadderSem().acquire();
        try {
          ranker = rankerService.createNewActiveRankerForAccountOnLadder(account, 1);
        } finally {
          rankerService.getLadderSem().release();
        }
      }

      if (account.getAccessRole().equals(AccountAccessRole.OWNER) || account.getAccessRole()
          .equals(AccountAccessRole.MODERATOR)
          || number
          == FairController.BASE_ASSHOLE_LADDER + accountService.findMaxTimesAsshole()
          || number <= ranker.getLadder().getNumber()) {
        LadderViewDTO l = rankerService.findAllRankerByLadderAreaAndAccount(number,
            account);
        wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, l);
      } else {
        wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION,
            "Can't get permission to view the ladder.",
            HttpStatus.FORBIDDEN);
      }

    } catch (IllegalArgumentException e) {
      wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      wsUtils.convertAndSendToUser(sha, LADDER_DESTINATION, e.getMessage(),
          HttpStatus.INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/ladder/post/bias")
  public void buyBias(SimpMessageHeaderAccessor sha, WSObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] BIAS: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
      rankerService.addEvent(num, new Event(EventType.BIAS, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/ladder/post/multi")
  public void buyMulti(SimpMessageHeaderAccessor sha, WSObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] MULTI: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
      rankerService.addEvent(num, new Event(EventType.MULTI, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/ladder/post/vinegar")
  public void throwVinegar(SimpMessageHeaderAccessor sha, WSObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] VINEGAR: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
      rankerService.addEvent(num, new Event(EventType.VINEGAR, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/ladder/post/promote")
  public void promote(SimpMessageHeaderAccessor sha, WSObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] PROMOTE: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
      rankerService.addEvent(num, new Event(EventType.PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/ladder/post/asshole")
  public void beAsshole(SimpMessageHeaderAccessor sha, WSObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] ASSHOLE: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
      rankerService.addEvent(num, new Event(EventType.ASSHOLE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }

  @MessageMapping("/ladder/post/auto-promote")
  public void buyAutoPromote(SimpMessageHeaderAccessor sha, WSObservedMessage wsMessage) {
    try {
      String uuid = StringEscapeUtils.escapeJava(wsMessage.getUuid());
      AccountEntity account = accountService.findAccountByUUID(UUID.fromString(uuid));
      if (account == null || account.getAccessRole()
          .equals(AccountAccessRole.BANNED_PLAYER)) {
        return;
      }
      Integer num = rankerService.findHighestActiveRankerByAccount(account).getLadder()
          .getNumber();
      log.info("[L{}] AUTOPROMOTE: {} (#{}) {}", num, account.getUsername(), account.getId(),
          wsMessage.getEvent());
      ModServerMessageData data = new ModServerMessageData(account.getId(),
          sha.getDestination(),
          wsMessage.getContent(), wsMessage.getEvent());
      wsUtils.convertAndSendToAll(ModerationController.GAME_UPDATE_DESTINATION + num, data);
      rankerService.addEvent(num, new Event(EventType.AUTO_PROMOTE, account.getId()));
    } catch (Exception e) {
      log.error(e.getMessage());
      e.printStackTrace();
    }
  }
}
